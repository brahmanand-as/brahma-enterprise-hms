package com.brahma.arogya.billing.service;

import com.brahma.arogya.billing.dto.FinancialAdjustmentRequest;
import com.brahma.arogya.billing.dto.FinancialCreditReconcileRequest;
import com.brahma.arogya.billing.dto.FinancialPartRequest;
import com.brahma.arogya.billing.dto.FinancialRefundRequest;
import com.brahma.arogya.billing.dto.FinancialSplitRequest;
import com.brahma.arogya.billing.repository.FinancialJdbcRepository;
import java.math.*;
import java.security.Principal;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@org.springframework.stereotype.Service()
public class FinancialService {

  private final FinancialJdbcRepository db;

  private final RevenueLedger ledger;

  public FinancialService(FinancialJdbcRepository db, RevenueLedger ledger) {
    this.db = db;
    this.ledger = ledger;
  }

  public List<Map<String, Object>> invoices() {
    return db.findInvoicesForInvoices();
  }

  public Map<String, Object> invoice(long id) {
    var row = db.findInvoicesForInvoice(id);
    row.put("insurancePending", ledger.insurancePending(id));
    row.put(
        "patientOutstanding",
        ((BigDecimal) row.get("total"))
            .subtract((BigDecimal) row.get("paid"))
            .subtract(ledger.insurancePending(id))
            .max(BigDecimal.ZERO));
    row.put("items", db.findInvoiceItemsForInvoice(id));
    row.put("payments", db.findPaymentsForInvoice(id));
    row.put("adjustments", db.findInvoiceAdjustmentsForInvoice(id));
    row.put("refunds", db.findRefundsForInvoice(id));
    return row;
  }

  @Transactional
  public Map<String, Object> pay(long id, FinancialSplitRequest r, Principal p) {
    var i = db.findInvoicesForPay(id);
    if (r.parts() == null || r.parts().isEmpty()) throw bad("At least one payment required");
    BigDecimal sum = BigDecimal.ZERO;
    for (FinancialPartRequest part : r.parts()) {
      sum = sum.add(RevenueLedger.money(part.amount(), false));
      if (part.method() == null || !Set.of("CASH", "CARD", "UPI", "BANK").contains(part.method()))
        throw bad("Invalid payment method");
      if (!part.method().equals("CASH")
          && (!Boolean.TRUE.equals(part.verified())
              || part.referenceNo() == null
              || part.referenceNo().isBlank()))
        throw bad("Verify electronic payment and provide reference");
    }
    BigDecimal paid = (BigDecimal) i.get("paid"), total = (BigDecimal) i.get("total");
    if (paid.add(sum).add(ledger.insurancePending(id)).compareTo(total) > 0)
      throw conflict("Payment exceeds outstanding balance");
    for (FinancialPartRequest part : r.parts())
      db.insertPaymentsForPay(id, part.amount(), part.method(), part.referenceNo(), p.getName());
    db.updateInvoicesForPay(sum, id);
    ledger.status(id);
    return invoice(id);
  }

  @Transactional
  public Map<String, Object> adjust(long id, FinancialAdjustmentRequest f, Principal p) {
    RevenueLedger.money(f.amount(), false);
    if (f.type() == null
        || !Set.of("DISCOUNT", "CREDIT", "TAX").contains(f.type())
        || f.reason() == null
        || f.reason().isBlank()) throw bad("Adjustment type and reason required");
    var i = db.findInvoicesForAdjust(id);
    BigDecimal total = (BigDecimal) i.get("total"),
        paid = (BigDecimal) i.get("paid"),
        updated = f.type().equals("TAX") ? total.add(f.amount()) : total.subtract(f.amount());
    if (updated.signum() < 0) throw bad("Adjustment exceeds invoice total");
    if (updated.compareTo(paid.add(ledger.insurancePending(id))) < 0)
      throw conflict("Record the refund before reducing an already-paid invoice");
    db.insertInvoiceAdjustmentsForAdjust(id, f.type(), f.amount(), f.reason(), p.getName());
    db.updateInvoicesForAdjust(
        updated,
        f.type().equals("TAX") ? BigDecimal.ZERO : f.amount(),
        f.type().equals("TAX") ? f.amount() : BigDecimal.ZERO,
        id);
    ledger.status(id);
    return invoice(id);
  }

  @Transactional
  public Map<String, Object> refund(long id, FinancialRefundRequest f, Principal p) {
    RevenueLedger.money(f.amount(), false);
    if (f.method() == null
        || !Set.of("CASH", "UPI", "CARD", "BANK").contains(f.method())
        || f.referenceNo() == null
        || f.referenceNo().isBlank()
        || f.reason() == null
        || f.reason().isBlank()) throw bad("Refund method, reference and reason required");
    var i = db.findInvoicesForRefund(id);
    BigDecimal refundable = db.readPaymentsForRefund(id).subtract(db.readRefundsForRefund(id));
    if (refundable.compareTo(f.amount()) < 0) throw conflict("Refund exceeds net collected amount");
    db.insertRefundsForRefund(id, f.amount(), f.method(), f.referenceNo(), f.reason(), p.getName());
    db.updateInvoicesForRefund(f.amount(), id);
    ledger.status(id);
    return invoice(id);
  }

  public List<Map<String, Object>> credits() {
    return db.findReturnCreditRequestsForCredits();
  }

  @Transactional
  public Map<String, String> reconcileCredit(
      long id, FinancialCreditReconcileRequest f, Principal p) {
    var c = db.findReturnCreditRequestsForReconcileCredit(id);
    if (!"PENDING".equals(c.get("status"))) throw conflict("Return credit already reconciled");
    long invoice = ((Number) c.get("invoice_id")).longValue();
    var i = db.findInvoicesForReconcileCredit(invoice);
    BigDecimal credit = (BigDecimal) c.get("amount"),
        total = (BigDecimal) i.get("total"),
        paid = (BigDecimal) i.get("paid"),
        updated = total.subtract(credit);
    if (updated.signum() < 0) throw conflict("Credit exceeds remaining invoice amount");
    BigDecimal insurerPending = ledger.insurancePending(invoice);
    if (updated.compareTo(insurerPending) < 0)
      throw conflict("Revise the insurer allocation before reducing this insured bill");
    BigDecimal refund = paid.add(insurerPending).subtract(updated).max(BigDecimal.ZERO);
    BigDecimal patientRefundable =
        db.readPaymentsForReconcileCredit(invoice)
            .subtract(db.readRefundsForReconcileCredit(invoice));
    if (refund.compareTo(patientRefundable) > 0)
      throw conflict("Insurer reconciliation is required before this post-settlement credit");
    if (refund.signum() > 0) {
      if (f.method() == null
          || !Set.of("CASH", "UPI", "CARD", "BANK").contains(f.method())
          || f.referenceNo() == null
          || f.referenceNo().isBlank())
        throw bad("Verify and enter the refund reference before reconciling this paid return");
      db.insertRefundsForReconcileCredit(
          invoice, refund, f.method(), f.referenceNo(), c.get("reason"), p.getName());
    }
    db.insertInvoiceAdjustmentsForReconcileCredit(invoice, credit, c.get("reason"), p.getName());
    db.updateInvoicesForReconcileCredit(updated, refund, credit, invoice);
    ledger.status(invoice);
    db.updateReturnCreditRequestsForReconcileCredit(p.getName(), id);
    return Map.of("status", "APPLIED");
  }

  private ResponseStatusException bad(String s) {
    return new ResponseStatusException(HttpStatus.BAD_REQUEST, s);
  }

  private ResponseStatusException conflict(String s) {
    return new ResponseStatusException(HttpStatus.CONFLICT, s);
  }
}
