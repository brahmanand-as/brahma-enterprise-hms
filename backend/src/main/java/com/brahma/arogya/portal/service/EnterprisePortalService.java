package com.brahma.arogya.portal.service;

import com.brahma.arogya.appointment.service.SchedulingService;
import com.brahma.arogya.patient.entity.Patient;
import com.brahma.arogya.patient.service.PatientService;
import com.brahma.arogya.portal.dto.EnterprisePortalProfileRequest;
import com.brahma.arogya.portal.dto.EnterprisePortalRegistrationRequest;
import com.brahma.arogya.portal.dto.EnterprisePortalTicketRequest;
import com.brahma.arogya.portal.dto.EnterprisePortalTicketReviewRequest;
import com.brahma.arogya.portal.repository.EnterprisePortalJdbcRepository;
import com.brahma.arogya.security.service.AccessPolicy;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@org.springframework.stereotype.Service()
public class EnterprisePortalService {

  private final AccessPolicy access;

  private final EnterprisePortalJdbcRepository db;

  private final PatientService patients;

  private final PasswordEncoder encoder;

  private final SchedulingService scheduling;

  public EnterprisePortalService(
      EnterprisePortalJdbcRepository db,
      PatientService patients,
      PasswordEncoder encoder,
      SchedulingService scheduling,
      AccessPolicy access) {
    this.db = db;
    this.patients = patients;
    this.encoder = encoder;
    this.scheduling = scheduling;
    this.access = access;
  }

  @Transactional
  public Map<String, Object> register(EnterprisePortalRegistrationRequest r) {
    if (r.username() == null
        || !r.username().matches("[A-Za-z0-9._-]{3,80}")
        || r.password() == null
        || r.password().length() < 12)
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Username and password of at least 12 characters required");
    if (r.mobile() == null || !r.mobile().matches("[0-9]{10}"))
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ten-digit mobile number required");
    db.insertPatientIdentityLocksForRegister(r.mobile());
    db.findPatientIdentityLocksForRegister(r.mobile());
    if (db.countPatientsForRegister(
            r.mobile(), r.firstName(), r.lastName() == null ? "" : r.lastName())
        > 0)
      throw new ResponseStatusException(
          HttpStatus.CONFLICT,
          "An MRN already exists. Sign in or ask reception to restore access; do not register"
              + " again.");
    Patient p = new Patient();
    p.setFirstName(r.firstName());
    p.setLastName(r.lastName());
    p.setMobile(r.mobile());
    p = patients.create(p);
    db.insertStaffAccountsForRegister(r.username(), encoder.encode(r.password()), p.getId());
    return Map.of("mrn", p.getUhid(), "status", "REGISTRATION_FEE_REQUIRED");
  }

  public Map<String, String> updateProfile(EnterprisePortalProfileRequest f, Principal p) {
    if (f.mobile() == null || !f.mobile().matches("[0-9]{10}"))
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ten digit mobile required");
    db.updatePatientsForUpdateProfile(
        f.mobile(), f.address(), f.bloodGroup(), scheduling.patient(p));
    return Map.of("status", "UPDATED");
  }

  public Map<String, Object> profile(Principal p) {
    return db.findPatientsForProfile(scheduling.patient(p));
  }

  public List<Map<String, Object>> appointments(Principal p) {
    return db.findAppointmentsForAppointments(scheduling.patient(p));
  }

  public List<Map<String, Object>> invoices(Principal p) {
    return db.findInvoicesForInvoices(scheduling.patient(p));
  }

  public List<Map<String, Object>> prescriptions(Principal p) {
    return db.findPrescriptionsForPrescriptions(scheduling.patient(p));
  }

  public List<Map<String, Object>> reports(Principal p) {
    return db.findLabOrderItemsForReports(scheduling.patient(p));
  }

  @Transactional
  public Map<String, String> fee(Principal p) {
    long pid = scheduling.patient(p);
    db.insertRegistrationPaymentsForFee(pid);
    return Map.of("status", "COMPLETED", "notice", "SIMULATION ONLY — excluded from revenue");
  }

  public List<Map<String, Object>> tickets(
      Principal p, org.springframework.security.core.Authentication auth) {
    boolean team = access.has(auth, "helpdesk.manage");
    return team
        ? db.findHelpdeskTicketsForTickets()
        : db.findHelpdeskTicketsForTickets2(p.getName());
  }

  public Map<String, String> ticket(EnterprisePortalTicketRequest r, Principal p) {
    if (r.subject() == null
        || r.subject().isBlank()
        || r.description() == null
        || r.description().isBlank()
        || !Set.of("LOW", "NORMAL", "HIGH", "URGENT").contains(r.priority()))
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Subject, description and priority required");
    db.insertHelpdeskTicketsForTicket(r.subject(), r.description(), r.priority(), p.getName());
    db.insertHelpdeskTicketsForTicket2();
    return Map.of("status", "OPEN");
  }

  public Map<String, String> review(long id, EnterprisePortalTicketReviewRequest r) {
    if (!Set.of("OPEN", "ASSIGNED", "IN_PROGRESS", "ESCALATED", "RESOLVED", "CLOSED")
            .contains(r.status())
        || ((r.status().equals("RESOLVED") || r.status().equals("CLOSED"))
            && (r.resolution() == null || r.resolution().isBlank())))
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Valid status and resolution required");
    if (db.updateHelpdeskTicketsForReview(
            r.status(), r.assignedTo(), r.resolution(), r.department(), id)
        == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    return Map.of("status", r.status());
  }

  public Map<String, Object> analytics(String month) {
    LocalDate start;
    try {
      start = java.time.YearMonth.parse(month).atDay(1);
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Use YYYY-MM");
    }
    LocalDate end = start.plusMonths(1), previous = start.minusMonths(1);
    BigDecimal current = db.readPaymentsForAnalytics(start, end),
        prior = db.readPaymentsForAnalytics2(previous, start);
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("month", month);
    out.put("collections", current);
    out.put("previousCollections", prior);
    out.put("absoluteChange", current.subtract(prior));
    out.put(
        "percentageChange",
        prior.signum() == 0
            ? null
            : current
                .subtract(prior)
                .multiply(BigDecimal.valueOf(100))
                .divide(prior, 2, java.math.RoundingMode.HALF_UP));
    out.put("patients", db.countPatientsForAnalytics(start, end));
    out.put("encounters", db.countEncountersForAnalytics(start, end));
    out.put("dummyPaymentsIncluded", false);
    return out;
  }
}
