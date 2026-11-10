package com.brahma.arogya.hr.repository;

import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;

@org.springframework.stereotype.Repository()
public class StaffingJdbcRepository {

  private final JdbcTemplate jdbc;

  public StaffingJdbcRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public List<Map<String, Object>> findDutyRosterForRoster() {
    return jdbc.queryForList(
        "SELECT r.*,e.name,e.department FROM duty_roster r JOIN employees e ON e.id=r.employee_id"
            + " ORDER BY shift_date DESC,starts_at");
  }

  public int insertDutyRosterForShift(
      Long employeeId, LocalDate shiftDate, LocalTime startsAt, LocalTime endsAt, Object value5) {
    return jdbc.update(
        "INSERT INTO duty_roster(employee_id,shift_date,starts_at,ends_at,ends_on)"
            + " VALUES(?,?,?,?,?)",
        employeeId,
        shiftDate,
        startsAt,
        endsAt,
        value5);
  }

  public Map<String, Object> findEmployeesForPublishDoctorSlots(Long employeeId) {
    return jdbc.queryForMap("SELECT name,designation FROM employees WHERE id=?", employeeId);
  }

  public String readHospitalSettingsForPublishDoctorSlots() {
    return jdbc.queryForObject(
        "SELECT setting_value FROM hospital_settings WHERE"
            + " setting_key='consultation_slot_minutes'",
        String.class);
  }

  public int insertDoctorSlotLocksForPublishDoctorSlots(Object value1) {
    return jdbc.update("INSERT IGNORE INTO doctor_slot_locks VALUES(?)", value1);
  }

  public Map<String, Object> findDoctorSlotLocksForPublishDoctorSlots(Object value1) {
    return jdbc.queryForMap(
        "SELECT * FROM doctor_slot_locks WHERE doctor_name=? FOR UPDATE", value1);
  }

  public Integer countAppointmentSlotsForPublishDoctorSlots(
      Long employeeId, LocalDateTime value3, LocalDateTime start) {
    return jdbc.queryForObject(
        "SELECT COUNT(*) FROM appointment_slots WHERE doctor_employee_id=? AND starts_at<?"
            + " AND ends_at>?",
        Integer.class,
        employeeId,
        value3,
        start);
  }

  public int insertAppointmentSlotsForPublishDoctorSlots(
      Object name, Long employeeId, LocalDateTime start, LocalDateTime value4) {
    return jdbc.update(
        "INSERT INTO appointment_slots(doctor_name,doctor_employee_id,starts_at,ends_at)"
            + " VALUES(?,?,?,?)",
        name,
        employeeId,
        start,
        value4);
  }

  public Map<String, Object> findEmployeesForValidateShift(Long employee) {
    return jdbc.queryForMap(
        "SELECT id FROM employees WHERE id=? AND status='ACTIVE' FOR UPDATE", employee);
  }

  public Integer countLeaveRequestsForValidateShift(
      Long employee, LocalDate finalDay, LocalDate day) {
    return jdbc.queryForObject(
        "SELECT COUNT(*) FROM leave_requests WHERE employee_id=? AND status='APPROVED' AND"
            + " start_date<=? AND end_date>=?",
        Integer.class,
        employee,
        finalDay,
        day);
  }

  public Integer countDutyRosterForValidateShift(
      Long employee, LocalDateTime to, LocalDateTime from, Long ignore, Long ignore2) {
    return jdbc.queryForObject(
        "SELECT COUNT(*) FROM duty_roster WHERE employee_id=? AND status='SCHEDULED' AND"
            + " TIMESTAMP(shift_date,starts_at)<? AND"
            + " TIMESTAMP(COALESCE(ends_on,shift_date),ends_at)>? AND (? IS NULL OR id<>?)",
        Integer.class,
        employee,
        to,
        from,
        ignore,
        ignore2);
  }

  public List<Map<String, Object>> findEmployeesForRequests(String name) {
    return jdbc.queryForList(
        "SELECT s.*,e.name replacement_name,r.shift_date,r.starts_at,r.ends_at FROM"
            + " shift_requests s JOIN employees e ON e.id=s.replacement_employee_id JOIN"
            + " duty_roster r ON r.id=s.roster_id JOIN employees original ON"
            + " original.id=r.employee_id JOIN department_heads h ON"
            + " h.department=original.department WHERE h.username=? ORDER BY s.id DESC",
        name);
  }

  public List<Map<String, Object>> findEmployeesForRequests2() {
    return jdbc.queryForList(
        "SELECT s.*,e.name replacement_name,r.shift_date,r.starts_at,r.ends_at FROM"
            + " shift_requests s JOIN employees e ON e.id=s.replacement_employee_id JOIN"
            + " duty_roster r ON r.id=s.roster_id ORDER BY s.id DESC");
  }

  public List<Map<String, Object>> findShiftRequestsForRequests(String name) {
    return jdbc.queryForList(
        "SELECT * FROM shift_requests WHERE requested_by=? ORDER BY id DESC", name);
  }

  public Map<String, Object> findDutyRosterForSwap(Long rosterId) {
    return jdbc.queryForMap("SELECT * FROM duty_roster WHERE id=?", rosterId);
  }

  public Long readStaffAccountsForSwap(String name) {
    return jdbc.queryForObject(
        "SELECT employee_id FROM staff_accounts WHERE username=?", Long.class, name);
  }

  public int insertShiftRequestsForSwap(
      Long rosterId, Long replacementEmployeeId, String reason, String name) {
    return jdbc.update(
        "INSERT INTO shift_requests(roster_id,replacement_employee_id,reason,requested_by)"
            + " VALUES(?,?,?,?)",
        rosterId,
        replacementEmployeeId,
        reason,
        name);
  }

  public Map<String, Object> findShiftRequestsForReview(long id) {
    return jdbc.queryForMap("SELECT * FROM shift_requests WHERE id=? FOR UPDATE", id);
  }

  public Map<String, Object> findDutyRosterForReview(Object rosterId) {
    return jdbc.queryForMap("SELECT * FROM duty_roster WHERE id=? FOR UPDATE", rosterId);
  }

  public Map<String, Object> findEmployeesForReview(Object employeeId) {
    return jdbc.queryForMap("SELECT department,designation FROM employees WHERE id=?", employeeId);
  }

  public Map<String, Object> findEmployeesForReview2(long replacement) {
    return jdbc.queryForMap("SELECT department,designation FROM employees WHERE id=?", replacement);
  }

  public String readEmployeesForReview(long replacement) {
    return jdbc.queryForObject("SELECT name FROM employees WHERE id=?", String.class, replacement);
  }

  public int insertDoctorSlotLocksForReview(String value1) {
    return jdbc.update("INSERT IGNORE INTO doctor_slot_locks VALUES(?)", value1);
  }

  public Map<String, Object> findDoctorSlotLocksForReview(String value1) {
    return jdbc.queryForMap(
        "SELECT * FROM doctor_slot_locks WHERE doctor_name=? FOR UPDATE", value1);
  }

  public Integer countAppointmentSlotsForReview(
      long replacement, long oldId, LocalDateTime value4, LocalDateTime value5) {
    return jdbc.queryForObject(
        "SELECT COUNT(*) FROM appointment_slots a JOIN appointment_slots b ON"
            + " b.doctor_employee_id=? AND a.starts_at<b.ends_at AND a.ends_at>b.starts_at"
            + " WHERE a.doctor_employee_id=? AND a.starts_at>=? AND a.starts_at<?",
        Integer.class,
        replacement,
        oldId,
        value4,
        value5);
  }

  public int updateAppointmentSlotsForReview(
      long replacement, String name, long oldId, LocalDateTime value4, LocalDateTime value5) {
    return jdbc.update(
        "UPDATE appointment_slots SET doctor_employee_id=?,doctor_name=?,blocked=0 WHERE"
            + " doctor_employee_id=? AND starts_at>=? AND starts_at<?",
        replacement,
        name,
        oldId,
        value4,
        value5);
  }

  public int updateAppointmentsForReview(
      long replacement, String name, long oldId, LocalDateTime value4, LocalDateTime value5) {
    return jdbc.update(
        "UPDATE appointments SET doctor_employee_id=?,doctor_name=? WHERE doctor_employee_id=?"
            + " AND appointment_at>=? AND appointment_at<? AND status='BOOKED'",
        replacement,
        name,
        oldId,
        value4,
        value5);
  }

  public int updateRescheduleRequestsForReview(
      String name, long replacement, LocalDateTime value3, LocalDateTime value4) {
    return jdbc.update(
        "UPDATE reschedule_requests r JOIN appointments a ON a.id=r.appointment_id SET"
            + " r.status='RESOLVED',r.resolved_by=?,r.resolved_at=NOW() WHERE r.status='PENDING'"
            + " AND a.doctor_employee_id=? AND a.appointment_at>=? AND a.appointment_at<?",
        name,
        replacement,
        value3,
        value4);
  }

  public List<Long> findAppointmentsForReview(
      long replacement, LocalDateTime value3, LocalDateTime value4) {
    return jdbc.queryForList(
        "SELECT patient_id FROM appointments WHERE doctor_employee_id=? AND appointment_at>=?"
            + " AND appointment_at<? AND status='BOOKED'",
        Long.class,
        replacement,
        value3,
        value4);
  }

  public int updateDutyRosterForReview(Object id) {
    return jdbc.update("UPDATE duty_roster SET status='REPLACED' WHERE id=?", id);
  }

  public int insertDutyRosterForReview(
      long replacement, LocalDate day, LocalTime start, LocalTime end, LocalDate ends, Object id) {
    return jdbc.update(
        "INSERT INTO"
            + " duty_roster(employee_id,shift_date,starts_at,ends_at,ends_on,replacement_for)"
            + " VALUES(?,?,?,?,?,?)",
        replacement,
        day,
        start,
        end,
        ends,
        id);
  }

  public int updateShiftRequestsForReview(String status, String name, long id) {
    return jdbc.update(
        "UPDATE shift_requests SET status=?,reviewed_by=?,reviewed_at=NOW() WHERE id=?",
        status,
        name,
        id);
  }

  public String readEmployeesForHead(long employee) {
    return jdbc.queryForObject(
        "SELECT department FROM employees WHERE id=?", String.class, employee);
  }

  public List<String> findDepartmentHeadsForHead(String dept) {
    return jdbc.queryForList(
        "SELECT username FROM department_heads WHERE department=?", String.class, dept);
  }

  public Map<String, Object> findStaffAccountsForHead(String username) {
    return jdbc.queryForMap(
        "SELECT username FROM staff_accounts WHERE username=? AND enabled=1", username);
  }

  public int insertDepartmentHeadsForHead(String department, String username) {
    return jdbc.update(
        "INSERT INTO department_heads VALUES(?,?) ON DUPLICATE KEY UPDATE"
            + " username=VALUES(username)",
        department,
        username);
  }

  public List<Map<String, Object>> findDepartmentHeadsForHeads() {
    return jdbc.queryForList("SELECT * FROM department_heads");
  }

  public int updateDutyRosterForApprovedLeave(long employee, LocalDate to, LocalDate from) {
    return jdbc.update(
        "UPDATE duty_roster SET status='ON_LEAVE' WHERE employee_id=? AND shift_date<=? AND"
            + " COALESCE(ends_on,shift_date)>=? AND status='SCHEDULED'",
        employee,
        to,
        from);
  }

  public int updateAppointmentSlotsForApprovedLeave(long employee, LocalDate from, LocalDate to) {
    return jdbc.update(
        "UPDATE appointment_slots SET blocked=1 WHERE doctor_employee_id=? AND DATE(starts_at)"
            + " BETWEEN ? AND ?",
        employee,
        from,
        to);
  }

  public int insertRescheduleRequestsForApprovedLeave(long employee, LocalDate from, LocalDate to) {
    return jdbc.update(
        "INSERT INTO reschedule_requests(appointment_id,requested_by,reason) SELECT"
            + " a.id,'SYSTEM','Doctor on approved leave: coordinate replacement or reschedule' FROM"
            + " appointments a WHERE a.doctor_employee_id=? AND DATE(a.appointment_at) BETWEEN ?"
            + " AND ? AND a.status='BOOKED' AND NOT EXISTS(SELECT 1 FROM reschedule_requests r"
            + " WHERE r.appointment_id=a.id AND r.status='PENDING')",
        employee,
        from,
        to);
  }

  public int insertAttendanceForAttendance(
      Long employeeId, LocalDate workDate, String status, BigDecimal hoursWorked) {
    return jdbc.update(
        "INSERT INTO attendance(employee_id,work_date,status,hours_worked) VALUES(?,?,?,?) ON"
            + " DUPLICATE KEY UPDATE status=VALUES(status),hours_worked=VALUES(hours_worked)",
        employeeId,
        workDate,
        status,
        hoursWorked);
  }

  public List<Map<String, Object>> findSalarySlipsForPayroll() {
    return jdbc.queryForList(
        "SELECT s.*,e.name FROM salary_slips s JOIN employees e ON e.id=s.employee_id ORDER BY"
            + " salary_month DESC");
  }

  public List<Map<String, Object>> findAttendanceForAttendance() {
    return jdbc.queryForList(
        "SELECT a.*,e.name FROM attendance a JOIN employees e ON e.id=a.employee_id ORDER BY"
            + " work_date DESC");
  }

  public int updateSalarySlipsForReconcile(String referenceNo, String name, long id) {
    return jdbc.update(
        "UPDATE salary_slips SET"
            + " payment_status='PAID',bank_reference=?,reconciled_by=?,credited_at=NOW() WHERE"
            + " id=? AND payment_status='PENDING'",
        referenceNo,
        name,
        id);
  }
}
