package com.brahma.arogya.appointment.repository;

import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;

@org.springframework.stereotype.Repository()
public class AppointmentManagementJdbcRepository {

  private final JdbcTemplate jdbc;

  public AppointmentManagementJdbcRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Map<String, Object> findAppointmentsForCancel(long id) {
    return jdbc.queryForMap("SELECT * FROM appointments WHERE id=? FOR UPDATE", id);
  }

  public int deleteSlotReservationsForCancel(long id) {
    return jdbc.update("DELETE FROM slot_reservations WHERE appointment_id=?", id);
  }

  public int updateAppointmentsForCancel(long id) {
    return jdbc.update("UPDATE appointments SET status='CANCELLED' WHERE id=?", id);
  }

  public int updateRescheduleRequestsForCancel(String name, long id) {
    return jdbc.update(
        "UPDATE reschedule_requests SET status='CANCELLED',resolved_by=?,resolved_at=NOW() WHERE"
            + " appointment_id=? AND status='PENDING'",
        name,
        id);
  }

  public int insertAppointmentsForProvisional(Long patientId, String doctorName, String reason) {
    return jdbc.update(
        "INSERT INTO"
            + " appointments(patient_id,doctor_name,appointment_at,type,status,reason,created_by_source)"
            + " VALUES(?,?,DATE_ADD(NOW(),INTERVAL 1"
            + " DAY),'OPD','PENDING',?,'RECEPTION_PROVISIONAL')",
        patientId,
        doctorName,
        reason);
  }

  public Long readGeneratedIdForProvisional() {
    return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
  }

  public int insertRescheduleRequestsForProvisional(long id, String name, Object value3) {
    return jdbc.update(
        "INSERT INTO reschedule_requests(appointment_id,requested_by,reason) VALUES(?,?,?)",
        id,
        name,
        value3);
  }

  public Map<String, Object> findRescheduleRequestsForResolve(long id) {
    return jdbc.queryForMap("SELECT * FROM reschedule_requests WHERE id=? FOR UPDATE", id);
  }

  public Map<String, Object> findAppointmentsForResolve(long aid) {
    return jdbc.queryForMap("SELECT * FROM appointments WHERE id=? FOR UPDATE", aid);
  }

  public int updateAppointmentsForResolve(long aid) {
    return jdbc.update("UPDATE appointments SET status='BOOKED' WHERE id=?", aid);
  }

  public int updateRescheduleRequestsForResolve(String name, long id) {
    return jdbc.update(
        "UPDATE reschedule_requests SET status='RESOLVED',resolved_by=?,resolved_at=NOW() WHERE"
            + " id=?",
        name,
        id);
  }
}
