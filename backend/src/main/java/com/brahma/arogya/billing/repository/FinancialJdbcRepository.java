package com.brahma.arogya.billing.repository;

import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;

@org.springframework.stereotype.Repository()
public class FinancialJdbcRepository {

  private final JdbcTemplate jdbc;

  public FinancialJdbcRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public List<Map<String, Object>> findInvoicesForInvoices() {
    return jdbc.queryForList(
        "SELECT i.*,GREATEST(i.total-i.paid-COALESCE((SELECT SUM(a.amount-a.settled) FROM"
            + " insurance_invoice_allocations a WHERE a.invoice_id=i.id),0),0)"
            + " patient_outstanding,CONCAT(p.first_name,' ',COALESCE(p.last_name,''))"
            + " patient_name,p.uhid FROM invoices i JOIN patients p ON p.id=i.patient_id ORDER BY"
            + " i.id DESC");
  }

  public Map<String, Object> findInvoicesForInvoice(long id) {
    return jdbc.queryForMap("SELECT * FROM invoices WHERE id=?", id);
  }

  public List<Map<String, Object>> findInvoiceItemsForInvoice(long id) {
    return jdbc.queryForList("SELECT * FROM invoice_items WHERE invoice_id=?", id);
  }

  public List<Map<String, Object>> findPaymentsForInvoice(long id) {
    return jdbc.queryForList("SELECT * FROM payments WHERE invoice_id=?", id);
  }

  public List<Map<String, Object>> findInvoiceAdjustmentsForInvoice(long id) {
    return jdbc.queryForList("SELECT * FROM invoice_adjustments WHERE invoice_id=?", id);
  }

  public List<Map<String, Object>> findRefundsForInvoice(long id) {
    return jdbc.queryForList("SELECT * FROM refunds WHERE invoice_id=?", id);
  }

  public Map<String, Object> findInvoicesForPay(long id) {
    return jdbc.queryForMap("SELECT * FROM invoices WHERE id=? FOR UPDATE", id);
  }

  public int insertPaymentsForPay(
      long id, BigDecimal amount, String method, String referenceNo, String name) {
    return jdbc.update(
        "INSERT INTO payments(invoice_id,amount,method,reference_no,verified_by,verified_at)"
            + " VALUES(?,?,?,?,?,NOW())",
        id,
        amount,
        method,
        referenceNo,
        name);
  }

  public int updateInvoicesForPay(BigDecimal sum, long id) {
    return jdbc.update("UPDATE invoices SET paid=paid+? WHERE id=?", sum, id);
  }

  public Map<String, Object> findInvoicesForAdjust(long id) {
    return jdbc.queryForMap("SELECT * FROM invoices WHERE id=? FOR UPDATE", id);
  }

  public int insertInvoiceAdjustmentsForAdjust(
      long id, String type, BigDecimal amount, String reason, String name) {
    return jdbc.update(
        "INSERT INTO invoice_adjustments(invoice_id,adjustment_type,amount,reason,performed_by)"
            + " VALUES(?,?,?,?,?)",
        id,
        type,
        amount,
        reason,
        name);
  }

  public int updateInvoicesForAdjust(BigDecimal updated, Object value2, Object value3, long id) {
    return jdbc.update(
        "UPDATE invoices SET total=?,discount_amount=discount_amount+?,tax_amount=tax_amount+?"
            + " WHERE id=?",
        updated,
        value2,
        value3,
        id);
  }

  public Map<String, Object> findInvoicesForRefund(long id) {
    return jdbc.queryForMap("SELECT * FROM invoices WHERE id=? FOR UPDATE", id);
  }

  public BigDecimal readPaymentsForRefund(long id) {
    return jdbc.queryForObject(
        "SELECT COALESCE(SUM(amount),0) FROM payments WHERE invoice_id=? AND"
            + " method<>'INSURANCE'",
        BigDecimal.class,
        id);
  }

  public BigDecimal readRefundsForRefund(long id) {
    return jdbc.queryForObject(
        "SELECT COALESCE(SUM(amount),0) FROM refunds WHERE invoice_id=?", BigDecimal.class, id);
  }

  public int insertRefundsForRefund(
      long id, BigDecimal amount, String method, String referenceNo, String reason, String name) {
    return jdbc.update(
        "INSERT INTO refunds(invoice_id,amount,method,reference_no,reason,performed_by)"
            + " VALUES(?,?,?,?,?,?)",
        id,
        amount,
        method,
        referenceNo,
        reason,
        name);
  }

  public int updateInvoicesForRefund(BigDecimal amount, long id) {
    return jdbc.update("UPDATE invoices SET paid=paid-? WHERE id=?", amount, id);
  }

  public List<Map<String, Object>> findReturnCreditRequestsForCredits() {
    return jdbc.queryForList(
        "SELECT c.*,i.invoice_no,p.uhid FROM return_credit_requests c JOIN invoices i ON"
            + " i.id=c.invoice_id JOIN patients p ON p.id=i.patient_id ORDER BY c.id DESC");
  }

  public Map<String, Object> findReturnCreditRequestsForReconcileCredit(long id) {
    return jdbc.queryForMap("SELECT * FROM return_credit_requests WHERE id=? FOR UPDATE", id);
  }

  public Map<String, Object> findInvoicesForReconcileCredit(long invoice) {
    return jdbc.queryForMap("SELECT * FROM invoices WHERE id=? FOR UPDATE", invoice);
  }

  public BigDecimal readPaymentsForReconcileCredit(long invoice) {
    return jdbc.queryForObject(
        "SELECT COALESCE(SUM(amount),0) FROM payments WHERE invoice_id=? AND"
            + " method<>'INSURANCE'",
        BigDecimal.class,
        invoice);
  }

  public BigDecimal readRefundsForReconcileCredit(long invoice) {
    return jdbc.queryForObject(
        "SELECT COALESCE(SUM(amount),0) FROM refunds WHERE invoice_id=? AND"
            + " method<>'INSURANCE'",
        BigDecimal.class,
        invoice);
  }

  public int insertRefundsForReconcileCredit(
      long invoice,
      BigDecimal refund,
      String method,
      String referenceNo,
      Object reason,
      String name) {
    return jdbc.update(
        "INSERT INTO refunds(invoice_id,amount,method,reference_no,reason,performed_by)"
            + " VALUES(?,?,?,?,?,?)",
        invoice,
        refund,
        method,
        referenceNo,
        reason,
        name);
  }

  public int insertInvoiceAdjustmentsForReconcileCredit(
      long invoice, BigDecimal credit, Object reason, String name) {
    return jdbc.update(
        "INSERT INTO invoice_adjustments(invoice_id,adjustment_type,amount,reason,performed_by)"
            + " VALUES(?,'CREDIT',?,?,?)",
        invoice,
        credit,
        reason,
        name);
  }

  public int updateInvoicesForReconcileCredit(
      BigDecimal updated, BigDecimal refund, BigDecimal credit, long invoice) {
    return jdbc.update(
        "UPDATE invoices SET total=?,paid=paid-?,discount_amount=discount_amount+? WHERE id=?",
        updated,
        refund,
        credit,
        invoice);
  }

  public int updateReturnCreditRequestsForReconcileCredit(String name, long id) {
    return jdbc.update(
        "UPDATE return_credit_requests SET status='APPLIED',reconciled_by=? WHERE id=?", name, id);
  }
}
