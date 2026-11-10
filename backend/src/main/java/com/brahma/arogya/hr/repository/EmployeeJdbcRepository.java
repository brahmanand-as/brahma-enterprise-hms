package com.brahma.arogya.hr.repository;

import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;

@org.springframework.stereotype.Repository()
public class EmployeeJdbcRepository {

  private final JdbcTemplate jdbc;

  public EmployeeJdbcRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Long readEmployeeNumberSequenceForCreate() {
    return jdbc.queryForObject(
        "SELECT next_value FROM employee_number_sequence WHERE id=1 FOR UPDATE", Long.class);
  }

  public int updateEmployeeNumberSequenceForCreate() {
    return jdbc.update("UPDATE employee_number_sequence SET next_value=next_value+1 WHERE id=1");
  }

  public int updateStaffAccountsForUpdate(Long id) {
    return jdbc.update(
        "UPDATE staff_accounts SET enabled=0,token_version=token_version+1 WHERE employee_id=?",
        id);
  }

  public int updateAppointmentSlotsForUpdate(Long id) {
    return jdbc.update(
        "UPDATE appointment_slots SET blocked=1 WHERE doctor_employee_id=? AND starts_at>NOW()",
        id);
  }

  public int insertRescheduleRequestsForUpdate(Long id) {
    return jdbc.update(
        "INSERT INTO reschedule_requests(appointment_id,requested_by,reason) SELECT"
            + " a.id,'SYSTEM','Doctor deactivated: coordinate replacement' FROM appointments a"
            + " WHERE a.doctor_employee_id=? AND a.appointment_at>NOW() AND a.status='BOOKED' AND"
            + " NOT EXISTS(SELECT 1 FROM reschedule_requests r WHERE r.appointment_id=a.id AND"
            + " r.status='PENDING')",
        id);
  }

  public Integer countStaffAccountsForDelete(Long id) {
    return jdbc.queryForObject(
        "SELECT COUNT(*) FROM staff_accounts WHERE employee_id=?", Integer.class, id);
  }

  public Integer countLeaveRequestsForDelete(Long id) {
    return jdbc.queryForObject(
        "SELECT COUNT(*) FROM leave_requests WHERE employee_id=?", Integer.class, id);
  }

  public Integer countSalarySlipsForDelete(Long id) {
    return jdbc.queryForObject(
        "SELECT COUNT(*) FROM salary_slips WHERE employee_id=?", Integer.class, id);
  }

  public Integer countDoctorProfilesForDelete(Long id) {
    return jdbc.queryForObject(
        "SELECT COUNT(*) FROM doctor_profiles WHERE employee_id=?", Integer.class, id);
  }
}
