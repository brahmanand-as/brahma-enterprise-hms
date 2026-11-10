package com.brahma.arogya.billing.repository;

import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;

@org.springframework.stereotype.Repository()
public class RevenueLedgerJdbcRepository {

  private final JdbcTemplate jdbc;

  public RevenueLedgerJdbcRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Map<String, Object> findPatientsForCharge(long patient) {
    return jdbc.queryForMap("SELECT id FROM patients WHERE id=? FOR UPDATE", patient);
  }

  public List<Long> findInvoiceItemsForCharge(String key) {
    return jdbc.queryForList(
        "SELECT invoice_id FROM invoice_items WHERE charge_key=?", Long.class, key);
  }

  public List<Long> findInvoicesForCharge(long patient, Long encounter, Long department) {
    return jdbc.queryForList(
        "SELECT id FROM invoices WHERE patient_id=? AND encounter_id <=> ? AND department_id"
            + " <=> ? AND status='UNPAID' ORDER BY id DESC LIMIT 1 FOR UPDATE",
        Long.class,
        patient,
        encounter,
        department);
  }

  public int insertInvoicesForCharge(String value1, long patient, Long encounter, Long department) {
    return jdbc.update(
        "INSERT INTO"
            + " invoices(invoice_no,patient_id,encounter_id,department_id,total,gross_total,paid,status)"
            + " VALUES(?,?,?,?,0,0,0,'UNPAID')",
        value1,
        patient,
        encounter,
        department);
  }

  public Long readGeneratedIdForCharge() {
    return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
  }

  public int insertInvoiceItemsForCharge2(
      long id,
      String type,
      long reference,
      String description,
      BigDecimal qty,
      BigDecimal price,
      BigDecimal amount,
      String key) {
    return jdbc.update(
        "INSERT INTO"
            + " invoice_items(invoice_id,item_type,reference_id,description,qty,unit_price,amount,charge_key)"
            + " VALUES(?,?,?,?,?,?,?,?)",
        id,
        type,
        reference,
        description,
        qty,
        price,
        amount,
        key);
  }

  public int updateInvoicesForCharge(BigDecimal amount, BigDecimal amount2, long id) {
    return jdbc.update(
        "UPDATE invoices SET total=total+?,gross_total=gross_total+? WHERE id=?",
        amount,
        amount2,
        id);
  }

  public List<Long> findDepartmentsForDepartment(String code) {
    return jdbc.queryForList("SELECT id FROM departments WHERE code=?", Long.class, code);
  }

  public Long readEncountersForDepartment(Long encounter) {
    return jdbc.queryForObject(
        "SELECT department_id FROM encounters WHERE id=?", Long.class, encounter);
  }

  public BigDecimal readInsuranceInvoiceAllocationsForInsurancePending(long invoice) {
    return jdbc.queryForObject(
        "SELECT COALESCE(SUM(amount-settled),0) FROM insurance_invoice_allocations WHERE"
            + " invoice_id=?",
        BigDecimal.class,
        invoice);
  }

  public Integer countInvoicesForCompleteVisit(long encounter) {
    return jdbc.queryForObject(
        "SELECT COUNT(*) FROM invoices WHERE encounter_id=? AND status<>'PAID'",
        Integer.class,
        encounter);
  }

  public int updateEncountersForCompleteVisit(long encounter) {
    return jdbc.update(
        "UPDATE encounters SET status='COMPLETED',completed_at=NOW() WHERE id=? AND"
            + " status='BILLING'",
        encounter);
  }

  public int updateAppointmentsForCompleteVisit(long encounter) {
    return jdbc.update(
        "UPDATE appointments a JOIN encounters e ON e.appointment_id=a.id SET"
            + " a.status='COMPLETED' WHERE e.id=? AND e.status='COMPLETED'",
        encounter);
  }

  public int updateInvoicesForStatus(long id) {
    return jdbc.update(
        "UPDATE invoices SET status=CASE WHEN paid>=total THEN 'PAID' WHEN"
            + " total-paid<=COALESCE((SELECT SUM(a.amount-a.settled) FROM"
            + " insurance_invoice_allocations a WHERE a.invoice_id=invoices.id),0) THEN"
            + " 'INSURANCE_PENDING' WHEN paid>0 THEN 'PARTIALLY_PAID' ELSE 'UNPAID' END WHERE id=?",
        id);
  }

  public Long readInvoicesForStatus(long id) {
    return jdbc.queryForObject("SELECT encounter_id FROM invoices WHERE id=?", Long.class, id);
  }
}
