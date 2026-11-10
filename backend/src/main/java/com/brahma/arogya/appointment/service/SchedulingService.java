package com.brahma.arogya.appointment.service;

import com.brahma.arogya.appointment.dto.SchedulingBookingRequest;
import com.brahma.arogya.appointment.dto.SchedulingRescheduleRequest;
import com.brahma.arogya.appointment.dto.SchedulingSlotRequest;
import com.brahma.arogya.appointment.repository.SchedulingJdbcRepository;
import com.brahma.arogya.notification.service.CoordinationService;
import com.brahma.arogya.security.service.CareScope;
import java.security.Principal;
import java.time.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@org.springframework.stereotype.Service()
public class SchedulingService {

  private final CareScope care;

  private final CoordinationService notices;

  private final SchedulingJdbcRepository db;

  public SchedulingService(
      SchedulingJdbcRepository db, CareScope care, CoordinationService notices) {
    this.db = db;
    this.care = care;
    this.notices = notices;
  }

  private ResponseStatusException bad(String s) {
    return new ResponseStatusException(HttpStatus.BAD_REQUEST, s);
  }

  public List<Map<String, Object>> slots() {
    return db.findAppointmentSlotsForSlots();
  }

  @Transactional
  public Map<String, String> slot(SchedulingSlotRequest r) {
    if (r.startsAt() == null
        || r.endsAt() == null
        || !r.endsAt().isAfter(r.startsAt())
        || !r.startsAt().isAfter(LocalDateTime.now()))
      throw bad("Future doctor time range required");
    long doctor =
        r.doctorEmployeeId() == null ? care.doctorId(r.doctorName()) : r.doctorEmployeeId();
    String name = db.readEmployeesForSlot(doctor);
    db.insertDoctorSlotLocksForSlot("DOCTOR-" + doctor);
    db.findDoctorSlotLocksForSlot("DOCTOR-" + doctor);
    if (db.countAppointmentSlotsForSlot(doctor, r.endsAt(), r.startsAt()) > 0)
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Doctor has an overlapping slot");
    db.insertAppointmentSlotsForSlot(name, doctor, r.startsAt(), r.endsAt());
    return Map.of("status", "CREATED");
  }

  public long patient(Principal p) {
    List<Long> ids = db.findStaffAccountsForPatient(p.getName());
    if (ids.isEmpty() || ids.get(0) == null)
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Patient account is not linked");
    return ids.get(0);
  }

  private void owned(long id, Principal p) {
    if (db.countAppointmentsForOwned(id, patient(p)) == 0)
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found");
  }

  @Transactional
  public Map<String, Object> book(
      SchedulingBookingRequest r, Principal p, jakarta.servlet.http.HttpServletRequest req) {
    Long pid = req.getRequestURI().contains("/patient/") ? patient(p) : r.patientId();
    if (pid == null || r.slotId() == null || db.countPatientsForBook(pid) == 0)
      throw bad("Valid patient and slot required");
    if (req.getRequestURI().contains("/patient/") && db.countRegistrationPaymentsForBook(pid) == 0)
      throw bad(
          "Complete registration fee demo before confirming self-service booking; contact reception"
              + " for provisional booking");
    Map<String, Object> slot = lock(r.slotId());
    db.insertAppointmentsForBook(
        pid,
        slot.get("doctor_name"),
        slot.get("starts_at"),
        r.reason(),
        r.slotId(),
        slot.get("doctor_employee_id"),
        care.departmentId(((Number) slot.get("doctor_employee_id")).longValue()));
    long id = db.readGeneratedIdForBook();
    db.insertSlotReservationsForBook(r.slotId(), id);
    audit(p, "BOOKED", id);
    if (notices != null)
      notices.patient(
          pid,
          "Appointment booked",
          "Your appointment with "
              + slot.get("doctor_name")
              + " is confirmed for "
              + slot.get("starts_at"));
    return db.findAppointmentsForBook(id);
  }

  private Map<String, Object> lock(long id) {
    var s = db.findAppointmentSlotsForLock(id);
    if (Boolean.TRUE.equals(s.get("blocked"))
        || ((Number) db.countSlotReservationsForLock(id)).intValue() > 0)
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Slot unavailable");
    if (db.countDutyRosterForLock(s.get("doctor_employee_id"), s.get("starts_at"), s.get("ends_at"))
        == 0)
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Doctor is not on an eligible scheduled duty");
    Object time = s.get("starts_at");
    LocalDateTime starts =
        time instanceof LocalDateTime dt ? dt : ((java.sql.Timestamp) time).toLocalDateTime();
    if (!starts.isAfter(LocalDateTime.now())) throw bad("Slot is in the past");
    return s;
  }

  @Transactional
  public Map<String, String> reschedule(
      long id,
      SchedulingRescheduleRequest r,
      Principal p,
      jakarta.servlet.http.HttpServletRequest req) {
    if (req.getRequestURI().contains("/patient/")) owned(id, p);
    var a = db.findAppointmentsForReschedule(id);
    if (!"BOOKED".equals(a.get("status")))
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Only booked appointments can be rescheduled");
    if (req.getRequestURI().contains("/patient/")) {
      Object time = a.get("appointment_at");
      LocalDateTime at =
          time instanceof LocalDateTime dt ? dt : ((java.sql.Timestamp) time).toLocalDateTime();
      long cutoff = Long.parseLong(db.readHospitalSettingsForReschedule());
      if (at.isBefore(LocalDateTime.now().plusMinutes(cutoff)))
        throw new ResponseStatusException(
            HttpStatus.CONFLICT, "Self-rescheduling cutoff reached; request reception assistance");
    }
    if (r.slotId() == null) throw bad("Select an available slot");
    var s = lock(r.slotId());
    db.deleteSlotReservationsForReschedule(id);
    db.insertSlotReservationsForReschedule(r.slotId(), id);
    db.updateAppointmentsForReschedule(
        r.slotId(), s.get("doctor_name"), s.get("starts_at"), s.get("doctor_employee_id"), id);
    audit(p, "RESCHEDULED", id);
    if (notices != null)
      notices.patient(
          ((Number) a.get("patient_id")).longValue(),
          "Appointment rescheduled",
          "New appointment: " + s.get("starts_at"));
    return Map.of("status", "BOOKED");
  }

  public Map<String, String> request(long id, SchedulingRescheduleRequest r, Principal p) {
    owned(id, p);
    if (r.reason() == null || r.reason().isBlank()) throw bad("Reason required");
    db.insertRescheduleRequestsForRequest(id, p.getName(), r.reason());
    return Map.of("status", "PENDING");
  }

  public List<Map<String, Object>> requests() {
    return db.findRescheduleRequestsForRequests();
  }

  private void audit(Principal p, String action, long id) {
    db.insertAuditLogsForAudit(p.getName(), action, String.valueOf(id));
  }
}
