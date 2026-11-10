package com.brahma.arogya.insurance.repository;

import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;

@org.springframework.stereotype.Repository()
public class InsuranceReconciliationJdbcRepository {

  private final JdbcTemplate jdbc;

  public InsuranceReconciliationJdbcRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public List<Map<String, Object>> findInsuranceEnhancementsForEnhancements() {
    return jdbc.queryForList(
        "SELECT e.*,c.provider FROM insurance_enhancements e JOIN insurance_claims c ON"
            + " c.id=e.claim_id ORDER BY e.id DESC");
  }

  public List<Map<String, Object>> findInsuranceClaimsForList() {
    return jdbc.queryForList(
        "SELECT c.*,p.uhid,p.first_name,a.ward,a.bed_no FROM insurance_claims c JOIN patients p"
            + " ON p.id=c.patient_id LEFT JOIN admissions a ON a.id=c.admission_id ORDER BY"
            + " c.id DESC");
  }

  public Map<String, Object> findInsuranceClaimsForDetail(long id) {
    return jdbc.queryForMap("SELECT * FROM insurance_claims WHERE id=?", id);
  }

  public List<Map<String, Object>> findInsuranceLinesForDetail(long id) {
    return jdbc.queryForList("SELECT * FROM insurance_lines WHERE claim_id=?", id);
  }

  public List<Map<String, Object>> findInsuranceSettlementsForDetail(long id) {
    return jdbc.queryForList("SELECT * FROM insurance_settlements WHERE claim_id=?", id);
  }

  public List<Map<String, Object>> findInsuranceEnhancementsForDetail(long id) {
    return jdbc.queryForList("SELECT * FROM insurance_enhancements WHERE claim_id=?", id);
  }

  public List<Map<String, Object>> findInsuranceInvoiceAllocationsForDetail(long id) {
    return jdbc.queryForList(
        "SELECT a.*,i.invoice_no FROM insurance_invoice_allocations a JOIN invoices i ON"
            + " i.id=a.invoice_id WHERE a.claim_id=?",
        id);
  }

  public List<Map<String, Object>> findInsuranceEventsForDetail(long id) {
    return jdbc.queryForList("SELECT * FROM insurance_events WHERE claim_id=? ORDER BY id", id);
  }

  public Map<String, Object> findInsuranceClaimsForLines(long id) {
    return jdbc.queryForMap("SELECT * FROM insurance_claims WHERE id=? FOR UPDATE", id);
  }

  public int deleteInsuranceLinesForLines(long id) {
    return jdbc.update("DELETE FROM insurance_lines WHERE claim_id=?", id);
  }

  public int insertInsuranceLinesForLines(
      long id, Object description, Object amount, Object eligibleAmount) {
    return jdbc.update(
        "INSERT INTO insurance_lines(claim_id,description,amount,eligible_amount)"
            + " VALUES(?,?,?,?)",
        id,
        description,
        amount,
        eligibleAmount);
  }

  public int updateInsuranceClaimsForLines(BigDecimal total, BigDecimal value2, long id) {
    return jdbc.update(
        "UPDATE insurance_claims SET claim_amount=?,non_payable=? WHERE id=?", total, value2, id);
  }

  public Map<String, Object> findInsuranceClaimsForEnhancement(long id) {
    return jdbc.queryForMap("SELECT * FROM insurance_claims WHERE id=? FOR UPDATE", id);
  }

  public int insertInsuranceEnhancementsForEnhancement(
      long id, BigDecimal amount, String notes, String name) {
    return jdbc.update(
        "INSERT INTO insurance_enhancements(claim_id,requested_amount,notes,requested_by)"
            + " VALUES(?,?,?,?)",
        id,
        amount,
        notes,
        name);
  }

  public int updateInsuranceClaimsForEnhancement(long id) {
    return jdbc.update("UPDATE insurance_claims SET status='ENHANCEMENT_REQUESTED' WHERE id=?", id);
  }

  public Map<String, Object> findInsuranceEnhancementsForReview(long id) {
    return jdbc.queryForMap("SELECT * FROM insurance_enhancements WHERE id=? FOR UPDATE", id);
  }

  public Map<String, Object> findInsuranceClaimsForReview(Object claimId) {
    return jdbc.queryForMap("SELECT * FROM insurance_claims WHERE id=? FOR UPDATE", claimId);
  }

  public int updateInsuranceClaimsForReview(BigDecimal newApproved, Object id) {
    return jdbc.update("UPDATE insurance_claims SET approved_amount=? WHERE id=?", newApproved, id);
  }

  public int updateInsuranceClaimsForReview2(Object id) {
    return jdbc.update("UPDATE insurance_claims SET status='APPROVED' WHERE id=?", id);
  }

  public int updateInsuranceEnhancementsForReview(
      String status, BigDecimal amount, String notes, String name, long id) {
    return jdbc.update(
        "UPDATE insurance_enhancements SET status=?,decision_amount=?,notes=?,reviewed_by=? WHERE"
            + " id=?",
        status,
        amount,
        notes,
        name,
        id);
  }

  public Map<String, Object> findInsuranceClaimsForAllocate(long id) {
    return jdbc.queryForMap("SELECT * FROM insurance_claims WHERE id=? FOR UPDATE", id);
  }

  public Map<String, Object> findInvoicesForAllocate(Long invoiceId) {
    return jdbc.queryForMap("SELECT * FROM invoices WHERE id=? FOR UPDATE", invoiceId);
  }

  public BigDecimal readInsuranceInvoiceAllocationsForAllocate(long id) {
    return jdbc.queryForObject(
        "SELECT COALESCE(SUM(amount),0) FROM insurance_invoice_allocations WHERE claim_id=?",
        BigDecimal.class,
        id);
  }

  public int insertInsuranceInvoiceAllocationsForAllocate(
      long id, Long invoiceId, BigDecimal amount) {
    return jdbc.update(
        "INSERT INTO insurance_invoice_allocations(claim_id,invoice_id,amount) VALUES(?,?,?)",
        id,
        invoiceId,
        amount);
  }

  public Map<String, Object> findInsuranceClaimsForSettle(long id) {
    return jdbc.queryForMap("SELECT * FROM insurance_claims WHERE id=? FOR UPDATE", id);
  }

  public List<Map<String, Object>> findInsuranceInvoiceAllocationsForSettle(long id) {
    return jdbc.queryForList(
        "SELECT * FROM insurance_invoice_allocations WHERE claim_id=? ORDER BY invoice_id FOR"
            + " UPDATE",
        id);
  }

  public Map<String, Object> findInvoicesForSettle(long invoice) {
    return jdbc.queryForMap("SELECT id FROM invoices WHERE id=? FOR UPDATE", invoice);
  }

  public int updateInsuranceInvoiceAllocationsForSettle(BigDecimal part, Object id) {
    return jdbc.update(
        "UPDATE insurance_invoice_allocations SET settled=settled+? WHERE id=?", part, id);
  }

  public int insertPaymentsForSettle(
      long invoice, BigDecimal part, String referenceNo, String name) {
    return jdbc.update(
        "INSERT INTO payments(invoice_id,amount,method,reference_no,verified_by,verified_at)"
            + " VALUES(?,?,'INSURANCE',?,?,NOW())",
        invoice,
        part,
        referenceNo,
        name);
  }

  public int updateInvoicesForSettle(BigDecimal part, long invoice) {
    return jdbc.update("UPDATE invoices SET paid=paid+? WHERE id=?", part, invoice);
  }

  public int insertInsuranceSettlementsForSettle(
      long id, BigDecimal amount, String referenceNo, LocalDate receivedOn, String name) {
    return jdbc.update(
        "INSERT INTO insurance_settlements(claim_id,amount,reference_no,received_on,performed_by)"
            + " VALUES(?,?,?,?,?)",
        id,
        amount,
        referenceNo,
        receivedOn,
        name);
  }

  public int updateInsuranceClaimsForSettle(BigDecimal updated, String status, long id) {
    return jdbc.update(
        "UPDATE insurance_claims SET settled_amount=?,status=? WHERE id=?", updated, status, id);
  }

  public int insertInsuranceEventsForEvent(
      long id, String s, BigDecimal amount, String notes, String name) {
    return jdbc.update(
        "INSERT INTO insurance_events(claim_id,status,amount,notes,performed_by) VALUES(?,?,?,?,?)",
        id,
        s,
        amount,
        notes,
        name);
  }
}
