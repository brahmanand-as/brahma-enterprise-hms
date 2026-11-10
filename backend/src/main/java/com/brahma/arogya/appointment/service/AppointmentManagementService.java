package com.brahma.arogya.appointment.service;

import com.brahma.arogya.appointment.dto.AppointmentManagementProvisionalRequest;
import com.brahma.arogya.appointment.dto.AppointmentManagementReasonRequest;
import com.brahma.arogya.appointment.dto.AppointmentManagementResolveRequest;
import com.brahma.arogya.appointment.dto.SchedulingRescheduleRequest;
import com.brahma.arogya.appointment.repository.AppointmentManagementJdbcRepository;
import com.brahma.arogya.notification.service.CoordinationService;
import java.security.Principal;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@org.springframework.stereotype.Service()
public class AppointmentManagementService {

  private final AppointmentManagementJdbcRepository db;

  private final SchedulingService scheduling;

  private final CoordinationService notices;

  public AppointmentManagementService(
      AppointmentManagementJdbcRepository db,
      SchedulingService scheduling,
      CoordinationService notices) {
    this.db = db;
    this.scheduling = scheduling;
    this.notices = notices;
  }

  @Transactional
  public Map<String, String> cancel(
      long id,
      AppointmentManagementReasonRequest r,
      Principal p,
      jakarta.servlet.http.HttpServletRequest req) {
    var a = db.findAppointmentsForCancel(id);
    if (req.getRequestURI().contains("/patient/")
        && ((Number) a.get("patient_id")).longValue() != scheduling.patient(p))
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    if (!Set.of("BOOKED", "PENDING").contains(a.get("status")))
      throw conflict("Only pending/booked appointments can be cancelled");
    if (r.reason() == null || r.reason().isBlank()) throw bad("Cancellation reason required");
    db.deleteSlotReservationsForCancel(id);
    db.updateAppointmentsForCancel(id);
    db.updateRescheduleRequestsForCancel(p.getName(), id);
    notices.patient(
        ((Number) a.get("patient_id")).longValue(), "Appointment cancelled", r.reason());
    return Map.of("status", "CANCELLED");
  }

  @Transactional
  public Map<String, String> provisional(AppointmentManagementProvisionalRequest f, Principal p) {
    if (f.patientId() == null || f.reason() == null || f.reason().isBlank())
      throw bad("Patient and reception notes required");
    db.insertAppointmentsForProvisional(f.patientId(), f.doctorName(), f.reason());
    long id = db.readGeneratedIdForProvisional();
    db.insertRescheduleRequestsForProvisional(
        id, p.getName(), "Provisional booking: " + f.reason());
    return Map.of("status", "PENDING");
  }

  @Transactional
  public Map<String, String> resolve(
      long id,
      AppointmentManagementResolveRequest f,
      Principal p,
      jakarta.servlet.http.HttpServletRequest req) {
    var r = db.findRescheduleRequestsForResolve(id);
    if (!"PENDING".equals(r.get("status"))) throw conflict("Request already resolved");
    long aid = ((Number) r.get("appointment_id")).longValue();
    var a = db.findAppointmentsForResolve(aid);
    if ("PENDING".equals(a.get("status"))) db.updateAppointmentsForResolve(aid);
    scheduling.reschedule(aid, new SchedulingRescheduleRequest(f.slotId(), f.reason()), p, req);
    db.updateRescheduleRequestsForResolve(p.getName(), id);
    notices.patient(
        ((Number) a.get("patient_id")).longValue(),
        "Appointment confirmed",
        "Reception confirmed your new appointment");
    return Map.of("status", "RESOLVED");
  }

  private ResponseStatusException bad(String s) {
    return new ResponseStatusException(HttpStatus.BAD_REQUEST, s);
  }

  private ResponseStatusException conflict(String s) {
    return new ResponseStatusException(HttpStatus.CONFLICT, s);
  }
}
