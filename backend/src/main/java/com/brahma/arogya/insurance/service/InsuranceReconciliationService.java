package com.brahma.arogya.insurance.service;

import com.brahma.arogya.billing.service.RevenueLedger;
import com.brahma.arogya.insurance.dto.InsuranceReconciliationAllocationRequest;
import com.brahma.arogya.insurance.dto.InsuranceReconciliationDecisionRequest;
import com.brahma.arogya.insurance.dto.InsuranceReconciliationEnhancementRequest;
import com.brahma.arogya.insurance.dto.InsuranceReconciliationLinesRequest;
import com.brahma.arogya.insurance.dto.InsuranceReconciliationSettlementRequest;
import com.brahma.arogya.insurance.repository.InsuranceReconciliationJdbcRepository;
import java.math.*;
import java.security.Principal;
import java.time.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@org.springframework.stereotype.Service()
public class InsuranceReconciliationService {

  private final RevenueLedger ledger;

  private final InsuranceReconciliationJdbcRepository db;

  public InsuranceReconciliationService(
      InsuranceReconciliationJdbcRepository db, RevenueLedger ledger) {
    this.db = db;
    this.ledger = ledger;
  }

  public List<Map<String, Object>> enhancements() {
    return db.findInsuranceEnhancementsForEnhancements();
  }

  public List<Map<String, Object>> list() {
    var rows = db.findInsuranceClaimsForList();
    for (var row : rows) summary(row);
    return rows;
  }

  public Map<String, Object> detail(long id) {
    var c = db.findInsuranceClaimsForDetail(id);
    summary(c);
    c.put("lines", db.findInsuranceLinesForDetail(id));
    c.put("settlements", db.findInsuranceSettlementsForDetail(id));
    c.put("enhancements", db.findInsuranceEnhancementsForDetail(id));
    c.put("allocations", db.findInsuranceInvoiceAllocationsForDetail(id));
    c.put("events", db.findInsuranceEventsForDetail(id));
    return c;
  }

  private void summary(Map<String, Object> c) {
    BigDecimal claim = (BigDecimal) c.get("claim_amount"),
        approved =
            c.get("approved_amount") == null
                ? BigDecimal.ZERO
                : (BigDecimal) c.get("approved_amount"),
        non = (BigDecimal) c.get("non_payable"),
        settled = (BigDecimal) c.get("settled_amount");
    c.put("eligibleAmount", claim.subtract(non).max(BigDecimal.ZERO));
    c.put("patientPayable", claim.subtract(approved).max(non));
    c.put("insurerOutstanding", approved.subtract(settled).max(BigDecimal.ZERO));
    c.put("reconciliationDifference", approved.subtract(settled));
  }

  @Transactional
  public Map<String, Object> lines(long id, InsuranceReconciliationLinesRequest f) {
    var c = db.findInsuranceClaimsForLines(id);
    if (!Set.of("DRAFT", "APPROVED").contains(c.get("status"))
        || f.lines() == null
        || f.lines().isEmpty()) throw conflict("Editable case and claim lines required");
    db.deleteInsuranceLinesForLines(id);
    BigDecimal total = BigDecimal.ZERO, eligible = BigDecimal.ZERO;
    for (var l : f.lines()) {
      RevenueLedger.money(l.amount(), true);
      RevenueLedger.money(l.eligibleAmount(), true);
      if (l.description() == null
          || l.description().isBlank()
          || l.eligibleAmount().compareTo(l.amount()) > 0)
        throw bad("Valid description and eligibility allocation required");
      total = total.add(l.amount());
      eligible = eligible.add(l.eligibleAmount());
      db.insertInsuranceLinesForLines(id, l.description(), l.amount(), l.eligibleAmount());
    }
    if (((BigDecimal) c.get("approved_amount")).compareTo(eligible) > 0)
      throw conflict("Allocation cannot be lower than an existing authorization");
    db.updateInsuranceClaimsForLines(total, total.subtract(eligible), id);
    return detail(id);
  }

  @Transactional
  public Map<String, String> enhancement(
      long id, InsuranceReconciliationEnhancementRequest f, Principal p) {
    RevenueLedger.money(f.amount(), false);
    var c = db.findInsuranceClaimsForEnhancement(id);
    if (!"APPROVED".equals(c.get("status"))) throw conflict("Approved authorization required");
    db.insertInsuranceEnhancementsForEnhancement(id, f.amount(), f.notes(), p.getName());
    db.updateInsuranceClaimsForEnhancement(id);
    return Map.of("status", "PENDING");
  }

  @Transactional
  public Map<String, String> review(
      long id, InsuranceReconciliationDecisionRequest f, Principal p) {
    if (f.status() == null || !Set.of("APPROVED", "REJECTED").contains(f.status()))
      throw bad("Approval decision required");
    RevenueLedger.money(f.amount(), true);
    var e = db.findInsuranceEnhancementsForReview(id);
    if (!"PENDING".equals(e.get("status"))
        || f.amount().compareTo((BigDecimal) e.get("requested_amount")) > 0)
      throw conflict("Pending enhancement and valid decision amount required");
    var c = db.findInsuranceClaimsForReview(e.get("claim_id"));
    if (f.status().equals("APPROVED")) {
      BigDecimal newApproved = ((BigDecimal) c.get("approved_amount")).add(f.amount());
      if (newApproved.compareTo(
              ((BigDecimal) c.get("claim_amount")).subtract((BigDecimal) c.get("non_payable")))
          > 0) throw bad("Enhancement exceeds eligible claim allocation");
      db.updateInsuranceClaimsForReview(newApproved, c.get("id"));
    }
    db.updateInsuranceClaimsForReview2(c.get("id"));
    db.updateInsuranceEnhancementsForReview(f.status(), f.amount(), f.notes(), p.getName(), id);
    event(
        ((Number) c.get("id")).longValue(), "ENHANCEMENT_" + f.status(), f.amount(), f.notes(), p);
    return Map.of("status", f.status());
  }

  @Transactional
  public Map<String, Object> allocate(long id, InsuranceReconciliationAllocationRequest f) {
    RevenueLedger.money(f.amount(), false);
    var c = db.findInsuranceClaimsForAllocate(id);
    if (!Set.of("APPROVED", "FINAL_SUBMITTED").contains(c.get("status"))
        || !"CASHLESS".equals(c.get("claim_type")))
      throw conflict("Approved cashless case required");
    var i = db.findInvoicesForAllocate(f.invoiceId());
    if (!c.get("patient_id").equals(i.get("patient_id")))
      throw conflict("Invoice patient does not match claim");
    BigDecimal allocated = db.readInsuranceInvoiceAllocationsForAllocate(id);
    if (allocated.add(f.amount()).compareTo((BigDecimal) c.get("approved_amount")) > 0)
      throw conflict("Invoice allocation exceeds insurer authorization");
    BigDecimal balance =
        ((BigDecimal) i.get("total"))
            .subtract((BigDecimal) i.get("paid"))
            .subtract(ledger.insurancePending(f.invoiceId()));
    if (f.amount().compareTo(balance) > 0) throw conflict("Allocation exceeds outstanding bill");
    db.insertInsuranceInvoiceAllocationsForAllocate(id, f.invoiceId(), f.amount());
    ledger.status(f.invoiceId());
    return detail(id);
  }

  @Transactional
  public Map<String, Object> settle(
      long id, InsuranceReconciliationSettlementRequest f, Principal p) {
    RevenueLedger.money(f.amount(), false);
    if (f.referenceNo() == null || f.referenceNo().isBlank() || f.receivedOn() == null)
      throw bad("Settlement reference and date required");
    var c = db.findInsuranceClaimsForSettle(id);
    if (!Set.of("FINAL_SUBMITTED", "PARTIALLY_SETTLED").contains(c.get("status")))
      throw conflict("Final submitted claim required");
    BigDecimal updated = ((BigDecimal) c.get("settled_amount")).add(f.amount()),
        approved = (BigDecimal) c.get("approved_amount");
    if (updated.compareTo(approved) > 0) throw conflict("Settlement exceeds authorization");
    if ("CASHLESS".equals(c.get("claim_type"))) {
      var allocations = db.findInsuranceInvoiceAllocationsForSettle(id);
      BigDecimal available =
          allocations.stream()
              .map(x -> ((BigDecimal) x.get("amount")).subtract((BigDecimal) x.get("settled")))
              .reduce(BigDecimal.ZERO, BigDecimal::add);
      if (available.compareTo(f.amount()) < 0)
        throw conflict("Link authorized amounts to invoices before reconciling cashless receipts");
      BigDecimal left = f.amount();
      for (var allocation : allocations) {
        BigDecimal part =
            left.min(
                ((BigDecimal) allocation.get("amount"))
                    .subtract((BigDecimal) allocation.get("settled")));
        if (part.signum() <= 0) continue;
        long invoice = ((Number) allocation.get("invoice_id")).longValue();
        db.findInvoicesForSettle(invoice);
        db.updateInsuranceInvoiceAllocationsForSettle(part, allocation.get("id"));
        db.insertPaymentsForSettle(invoice, part, f.referenceNo(), p.getName());
        db.updateInvoicesForSettle(part, invoice);
        ledger.status(invoice);
        left = left.subtract(part);
        if (left.signum() == 0) break;
      }
    }
    db.insertInsuranceSettlementsForSettle(
        id, f.amount(), f.referenceNo(), f.receivedOn(), p.getName());
    String status = updated.compareTo(approved) == 0 ? "SETTLED" : "PARTIALLY_SETTLED";
    db.updateInsuranceClaimsForSettle(updated, status, id);
    event(id, status, f.amount(), f.referenceNo(), p);
    return detail(id);
  }

  private void event(long id, String s, BigDecimal amount, String notes, Principal p) {
    db.insertInsuranceEventsForEvent(id, s, amount, notes, p.getName());
  }

  private ResponseStatusException bad(String s) {
    return new ResponseStatusException(HttpStatus.BAD_REQUEST, s);
  }

  private ResponseStatusException conflict(String s) {
    return new ResponseStatusException(HttpStatus.CONFLICT, s);
  }
}
