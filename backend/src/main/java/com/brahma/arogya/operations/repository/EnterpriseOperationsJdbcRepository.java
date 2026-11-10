package com.brahma.arogya.operations.repository;

import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;

@org.springframework.stereotype.Repository()
public class EnterpriseOperationsJdbcRepository {

  private final JdbcTemplate jdbc;

  public EnterpriseOperationsJdbcRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public List<Map<String, Object>> findDutyRosterForRoster() {
    return jdbc.queryForList(
        "SELECT r.*,e.name FROM duty_roster r JOIN employees e ON e.id=r.employee_id ORDER BY"
            + " shift_date DESC");
  }

  public Map<String, Object> findDutyRosterForAbsent(long id) {
    return jdbc.queryForMap(
        "SELECT r.*,e.name FROM duty_roster r JOIN employees e ON e.id=r.employee_id WHERE"
            + " r.id=? FOR UPDATE",
        id);
  }

  public int updateDutyRosterForAbsent(long id) {
    return jdbc.update("UPDATE duty_roster SET status='ABSENT' WHERE id=?", id);
  }

  public int updateAppointmentSlotsForAbsent(Object employeeId, Object shiftDate) {
    return jdbc.update(
        "UPDATE appointment_slots SET blocked=1 WHERE doctor_employee_id=? AND DATE(starts_at)=?",
        employeeId,
        shiftDate);
  }

  public int insertRescheduleRequestsForAbsent(String name, Object employeeId, Object shiftDate) {
    return jdbc.update(
        "INSERT INTO reschedule_requests(appointment_id,requested_by,reason) SELECT a.id,?,'Doctor"
            + " absent: reception to coordinate replacement' FROM appointments a WHERE"
            + " a.doctor_employee_id=? AND DATE(a.appointment_at)=? AND a.status='BOOKED'",
        name,
        employeeId,
        shiftDate);
  }

  public List<Map<String, Object>> findInsuranceClaimsForClaims() {
    return jdbc.queryForList("SELECT * FROM insurance_claims ORDER BY id DESC");
  }

  public Map<String, Object> findInsuranceClaimsForClaim(long id) {
    return jdbc.queryForMap("SELECT * FROM insurance_claims WHERE id=? FOR UPDATE", id);
  }

  public int updateInsuranceClaimsForClaim(
      String status,
      String status2,
      BigDecimal amount,
      String status3,
      BigDecimal amount2,
      long id) {
    return jdbc.update(
        "UPDATE insurance_claims SET status=?,approved_amount=CASE WHEN ?='APPROVED' THEN ? ELSE"
            + " approved_amount END,settled_amount=CASE WHEN ?='SETTLED' THEN ? ELSE settled_amount"
            + " END WHERE id=?",
        status,
        status2,
        amount,
        status3,
        amount2,
        id);
  }

  public List<Long> findInsuranceInvoiceAllocationsForClaim(long id) {
    return jdbc.queryForList(
        "SELECT invoice_id FROM insurance_invoice_allocations WHERE claim_id=?", Long.class, id);
  }

  public int updateInsuranceInvoiceAllocationsForClaim(long id) {
    return jdbc.update(
        "UPDATE insurance_invoice_allocations SET amount=settled WHERE claim_id=?", id);
  }

  public int updateInsuranceClaimsForClaim2(long id) {
    return jdbc.update("UPDATE insurance_claims SET approved_amount=settled_amount WHERE id=?", id);
  }

  public int insertInsuranceEventsForClaim(
      long id, String status, BigDecimal amount, String notes, String name) {
    return jdbc.update(
        "INSERT INTO insurance_events(claim_id,status,amount,notes,performed_by) VALUES(?,?,?,?,?)",
        id,
        status,
        amount,
        notes,
        name);
  }

  public Map<String, Object> findInsuranceClaimsForClaim2(long id) {
    return jdbc.queryForMap("SELECT * FROM insurance_claims WHERE id=?", id);
  }

  public Map<String, Object> findRadiologyOrdersForRadiology(long id) {
    return jdbc.queryForMap("SELECT * FROM radiology_orders WHERE id=? FOR UPDATE", id);
  }

  public int updateRadiologyOrdersForRadiology(
      String status, String report, String status2, String name, String status3, long id) {
    return jdbc.update(
        "UPDATE radiology_orders SET status=?,report=COALESCE(?,report),verified_by=CASE WHEN"
            + " ?='VERIFIED' THEN ? ELSE verified_by END,verified_at=CASE WHEN ?='VERIFIED' THEN"
            + " NOW() ELSE verified_at END WHERE id=?",
        status,
        report,
        status2,
        name,
        status3,
        id);
  }
}
