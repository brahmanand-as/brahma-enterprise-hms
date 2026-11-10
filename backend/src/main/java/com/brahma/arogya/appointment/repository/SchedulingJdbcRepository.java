package com.brahma.arogya.appointment.repository;

import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;

@org.springframework.stereotype.Repository()
public class SchedulingJdbcRepository {

  private final JdbcTemplate jdbc;

  public SchedulingJdbcRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public List<Map<String, Object>> findAppointmentSlotsForSlots() {
    return jdbc.queryForList(
        "SELECT s.* FROM appointment_slots s LEFT JOIN slot_reservations r ON r.slot_id=s.id WHERE"
            + " r.slot_id IS NULL AND s.blocked=0 AND s.starts_at>NOW() AND EXISTS(SELECT 1 FROM"
            + " duty_roster r WHERE r.employee_id=s.doctor_employee_id AND r.status='SCHEDULED' AND"
            + " s.starts_at>=TIMESTAMP(r.shift_date,r.starts_at) AND"
            + " s.ends_at<=TIMESTAMP(COALESCE(r.ends_on,r.shift_date),r.ends_at)) ORDER BY"
            + " s.starts_at LIMIT 200");
  }

  public String readEmployeesForSlot(long doctor) {
    return jdbc.queryForObject(
        "SELECT name FROM employees WHERE id=? AND designation='DOCTOR' AND status='ACTIVE'",
        String.class,
        doctor);
  }

  public int insertDoctorSlotLocksForSlot(String value1) {
    return jdbc.update("INSERT IGNORE INTO doctor_slot_locks VALUES(?)", value1);
  }

  public Map<String, Object> findDoctorSlotLocksForSlot(String value1) {
    return jdbc.queryForMap(
        "SELECT * FROM doctor_slot_locks WHERE doctor_name=? FOR UPDATE", value1);
  }

  public Integer countAppointmentSlotsForSlot(
      long doctor, LocalDateTime endsAt, LocalDateTime startsAt) {
    return jdbc.queryForObject(
        "SELECT COUNT(*) FROM appointment_slots WHERE doctor_employee_id=? AND starts_at<? AND"
            + " ends_at>?",
        Integer.class,
        doctor,
        endsAt,
        startsAt);
  }

  public int insertAppointmentSlotsForSlot(
      String name, long doctor, LocalDateTime startsAt, LocalDateTime endsAt) {
    return jdbc.update(
        "INSERT INTO appointment_slots(doctor_name,doctor_employee_id,starts_at,ends_at)"
            + " VALUES(?,?,?,?)",
        name,
        doctor,
        startsAt,
        endsAt);
  }

  public List<Long> findStaffAccountsForPatient(String name) {
    return jdbc.query(
        "SELECT patient_id FROM staff_accounts WHERE username=?",
        (rs, n) -> rs.getObject(1, Long.class),
        name);
  }

  public Integer countAppointmentsForOwned(long id, long value3) {
    return jdbc.queryForObject(
        "SELECT COUNT(*) FROM appointments WHERE id=? AND patient_id=?", Integer.class, id, value3);
  }

  public Integer countPatientsForBook(Long pid) {
    return jdbc.queryForObject("SELECT COUNT(*) FROM patients WHERE id=?", Integer.class, pid);
  }

  public Integer countRegistrationPaymentsForBook(Long pid) {
    return jdbc.queryForObject(
        "SELECT COUNT(*) FROM registration_payments WHERE patient_id=? AND" + " status='COMPLETED'",
        Integer.class,
        pid);
  }

  public int insertAppointmentsForBook(
      Long pid,
      Object doctorName,
      Object startsAt,
      String reason,
      Long slotId,
      Object doctorEmployeeId,
      Long value7) {
    return jdbc.update(
        "INSERT INTO"
            + " appointments(patient_id,doctor_name,appointment_at,type,status,reason,slot_id,doctor_employee_id,department_id)"
            + " VALUES(?,?,?,'OPD','BOOKED',?,?,?,?)",
        pid,
        doctorName,
        startsAt,
        reason,
        slotId,
        doctorEmployeeId,
        value7);
  }

  public Long readGeneratedIdForBook() {
    return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
  }

  public int insertSlotReservationsForBook(Long slotId, long id) {
    return jdbc.update("INSERT INTO slot_reservations VALUES(?,?)", slotId, id);
  }

  public Map<String, Object> findAppointmentsForBook(long id) {
    return jdbc.queryForMap("SELECT * FROM appointments WHERE id=?", id);
  }

  public Map<String, Object> findAppointmentSlotsForLock(long id) {
    return jdbc.queryForMap("SELECT * FROM appointment_slots WHERE id=? FOR UPDATE", id);
  }

  public Integer countSlotReservationsForLock(long id) {
    return jdbc.queryForObject(
        "SELECT COUNT(*) FROM slot_reservations WHERE slot_id=?", Integer.class, id);
  }

  public Integer countDutyRosterForLock(Object doctorEmployeeId, Object startsAt, Object endsAt) {
    return jdbc.queryForObject(
        "SELECT COUNT(*) FROM duty_roster WHERE employee_id=? AND status='SCHEDULED' AND"
            + " TIMESTAMP(shift_date,starts_at)<=? AND"
            + " TIMESTAMP(COALESCE(ends_on,shift_date),ends_at)>=?",
        Integer.class,
        doctorEmployeeId,
        startsAt,
        endsAt);
  }

  public Map<String, Object> findAppointmentsForReschedule(long id) {
    return jdbc.queryForMap("SELECT * FROM appointments WHERE id=? FOR UPDATE", id);
  }

  public String readHospitalSettingsForReschedule() {
    return jdbc.queryForObject(
        "SELECT setting_value FROM hospital_settings WHERE"
            + " setting_key='reschedule_cutoff_minutes'",
        String.class);
  }

  public int deleteSlotReservationsForReschedule(long id) {
    return jdbc.update("DELETE FROM slot_reservations WHERE appointment_id=?", id);
  }

  public int insertSlotReservationsForReschedule(Long slotId, long id) {
    return jdbc.update("INSERT INTO slot_reservations VALUES(?,?)", slotId, id);
  }

  public int updateAppointmentsForReschedule(
      Long slotId, Object doctorName, Object startsAt, Object doctorEmployeeId, long id) {
    return jdbc.update(
        "UPDATE appointments SET slot_id=?,doctor_name=?,appointment_at=?,doctor_employee_id=?"
            + " WHERE id=?",
        slotId,
        doctorName,
        startsAt,
        doctorEmployeeId,
        id);
  }

  public int insertRescheduleRequestsForRequest(long id, String name, String reason) {
    return jdbc.update(
        "INSERT INTO reschedule_requests(appointment_id,requested_by,reason) VALUES(?,?,?)",
        id,
        name,
        reason);
  }

  public List<Map<String, Object>> findRescheduleRequestsForRequests() {
    return jdbc.queryForList("SELECT * FROM reschedule_requests ORDER BY created_at DESC");
  }

  public int insertAuditLogsForAudit(String name, String action, String value3) {
    return jdbc.update(
        "INSERT INTO audit_logs(username,action,entity_type,entity_id) VALUES(?,?,'APPOINTMENT',?)",
        name,
        action,
        value3);
  }
}
