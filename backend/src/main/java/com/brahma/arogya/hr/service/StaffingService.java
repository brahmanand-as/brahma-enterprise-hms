package com.brahma.arogya.hr.service;

import com.brahma.arogya.hr.dto.StaffingAttendanceRequest;
import com.brahma.arogya.hr.dto.StaffingHeadRequest;
import com.brahma.arogya.hr.dto.StaffingReconcileRequest;
import com.brahma.arogya.hr.dto.StaffingReviewRequest;
import com.brahma.arogya.hr.dto.StaffingShiftRequest;
import com.brahma.arogya.hr.dto.StaffingSwapRequest;
import com.brahma.arogya.hr.repository.StaffingJdbcRepository;
import com.brahma.arogya.notification.service.CoordinationService;
import com.brahma.arogya.security.service.AccessPolicy;
import java.math.*;
import java.security.Principal;
import java.time.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@org.springframework.stereotype.Service()
public class StaffingService {

  private final StaffingJdbcRepository db;

  private final AccessPolicy access;

  private final CoordinationService notices;

  public StaffingService(
      StaffingJdbcRepository db, AccessPolicy access, CoordinationService notices) {
    this.db = db;
    this.access = access;
    this.notices = notices;
  }

  public List<Map<String, Object>> roster() {
    return db.findDutyRosterForRoster();
  }

  @Transactional
  public Map<String, String> shift(StaffingShiftRequest f) {
    validateShift(f.employeeId(), f.shiftDate(), f.startsAt(), f.endsAt(), f.endsOn(), null);
    db.insertDutyRosterForShift(
        f.employeeId(),
        f.shiftDate(),
        f.startsAt(),
        f.endsAt(),
        endDate(f.shiftDate(), f.startsAt(), f.endsAt(), f.endsOn()));
    publishDoctorSlots(f);
    return Map.of("status", "SCHEDULED");
  }

  private void publishDoctorSlots(StaffingShiftRequest f) {
    var e = db.findEmployeesForPublishDoctorSlots(f.employeeId());
    if (!"DOCTOR".equalsIgnoreCase(String.valueOf(e.get("designation")))) return;
    int minutes = Integer.parseInt(db.readHospitalSettingsForPublishDoctorSlots());
    if (minutes < 5 || minutes > 240) throw bad("Consultation duration must be 5 to 240 minutes");
    db.insertDoctorSlotLocksForPublishDoctorSlots("DOCTOR-" + f.employeeId());
    db.findDoctorSlotLocksForPublishDoctorSlots("DOCTOR-" + f.employeeId());
    LocalDateTime end =
        endDate(f.shiftDate(), f.startsAt(), f.endsAt(), f.endsOn()).atTime(f.endsAt());
    for (LocalDateTime start = f.shiftDate().atTime(f.startsAt());
        !start.plusMinutes(minutes).isAfter(end);
        start = start.plusMinutes(minutes)) {
      if (start.isBefore(LocalDateTime.now())) continue;
      if (db.countAppointmentSlotsForPublishDoctorSlots(
              f.employeeId(), start.plusMinutes(minutes), start)
          > 0) continue;
      db.insertAppointmentSlotsForPublishDoctorSlots(
          e.get("name"), f.employeeId(), start, start.plusMinutes(minutes));
    }
  }

  private LocalDate endDate(LocalDate day, LocalTime start, LocalTime end, LocalDate explicit) {
    return explicit != null ? explicit : end.isAfter(start) ? day : day.plusDays(1);
  }

  private void validateShift(
      Long employee, LocalDate day, LocalTime start, LocalTime end, LocalDate ends, Long ignore) {
    if (employee == null || day == null || start == null || end == null)
      throw bad("Employee and shift times required");
    LocalDate finalDay = endDate(day, start, end, ends);
    LocalDateTime from = day.atTime(start), to = finalDay.atTime(end);
    if (!to.isAfter(from) || Duration.between(from, to).toHours() > 24)
      throw bad("Shift must be between 1 minute and 24 hours");
    db.findEmployeesForValidateShift(employee);
    if (db.countLeaveRequestsForValidateShift(employee, finalDay, day) > 0)
      throw conflict("Employee on approved leave");
    if (db.countDutyRosterForValidateShift(employee, to, from, ignore, ignore) > 0)
      throw conflict("Shift overlaps existing duty");
  }

  public List<Map<String, Object>> requests(Principal p, Authentication auth) {
    boolean manager = access.has(auth, "roster.manage");
    if (!manager && access.has(auth, "roster.approve"))
      return db.findEmployeesForRequests(p.getName());
    return manager ? db.findEmployeesForRequests2() : db.findShiftRequestsForRequests(p.getName());
  }

  public Map<String, String> swap(StaffingSwapRequest f, Principal p, Authentication auth) {
    if (f.rosterId() == null
        || f.replacementEmployeeId() == null
        || f.reason() == null
        || f.reason().isBlank()) throw bad("Shift, replacement and reason required");
    var r = db.findDutyRosterForSwap(f.rosterId());
    Long self = db.readStaffAccountsForSwap(p.getName());
    if (!access.has(auth, "roster.manage") && !Objects.equals(self, r.get("employee_id")))
      throw forbidden();
    db.insertShiftRequestsForSwap(f.rosterId(), f.replacementEmployeeId(), f.reason(), p.getName());
    notices.department(
        "roster.manage",
        "Shift replacement approval",
        "A shift replacement request requires approval");
    return Map.of("status", "PENDING");
  }

  @Transactional
  public Map<String, String> review(
      long id, StaffingReviewRequest f, Principal p, Authentication auth) {
    if (f.status() == null || !Set.of("APPROVED", "REJECTED").contains(f.status()))
      throw bad("Decision required");
    var s = db.findShiftRequestsForReview(id);
    if (!"PENDING".equals(s.get("status"))) throw conflict("Request already reviewed");
    var r = db.findDutyRosterForReview(s.get("roster_id"));
    head(((Number) r.get("employee_id")).longValue(), p, auth);
    if (f.status().equals("APPROVED")) {
      long replacement = ((Number) s.get("replacement_employee_id")).longValue();
      var oldEmployee = db.findEmployeesForReview(r.get("employee_id"));
      var replacementEmployee = db.findEmployeesForReview2(replacement);
      if (!Objects.equals(oldEmployee.get("department"), replacementEmployee.get("department"))
          || !Objects.equals(
              oldEmployee.get("designation"), replacementEmployee.get("designation")))
        throw conflict("Replacement must match the department and professional designation");
      LocalDate day = date(r.get("shift_date")), ends = date(r.get("ends_on"));
      LocalTime start = time(r.get("starts_at")), end = time(r.get("ends_at"));
      validateShift(replacement, day, start, end, ends, null);
      if ("DOCTOR".equals(oldEmployee.get("designation"))) {
        String name = db.readEmployeesForReview(replacement);
        db.insertDoctorSlotLocksForReview("DOCTOR-" + replacement);
        db.findDoctorSlotLocksForReview("DOCTOR-" + replacement);
        long oldId = ((Number) r.get("employee_id")).longValue();
        if (db.countAppointmentSlotsForReview(
                replacement, oldId, day.atTime(start), ends.atTime(end))
            > 0) throw conflict("Replacement doctor has conflicting availability");
        db.updateAppointmentSlotsForReview(
            replacement, name, oldId, day.atTime(start), ends.atTime(end));
        db.updateAppointmentsForReview(
            replacement, name, oldId, day.atTime(start), ends.atTime(end));
      }
      db.updateRescheduleRequestsForReview(
          p.getName(), replacement, day.atTime(start), ends.atTime(end));
      for (Long patient :
          db.findAppointmentsForReview(replacement, day.atTime(start), ends.atTime(end)))
        notices.patient(
            patient,
            "Replacement doctor confirmed",
            "Your scheduled appointment is retained with the approved replacement clinician");
      db.updateDutyRosterForReview(r.get("id"));
      db.insertDutyRosterForReview(replacement, day, start, end, ends, r.get("id"));
    }
    db.updateShiftRequestsForReview(f.status(), p.getName(), id);
    notices.notify(
        String.valueOf(s.get("requested_by")),
        "Shift request " + f.status(),
        "Your replacement request has been reviewed");
    return Map.of("status", f.status());
  }

  public void head(long employee, Principal p, Authentication auth) {
    if (access.role(auth, "ADMIN")) return;
    String dept = db.readEmployeesForHead(employee);
    var heads = db.findDepartmentHeadsForHead(dept);
    if (!heads.isEmpty() && !heads.contains(p.getName())) throw forbidden();
    if (heads.isEmpty() && !access.has(auth, "hr.manage")) throw forbidden();
  }

  public Map<String, String> head(StaffingHeadRequest f) {
    db.findStaffAccountsForHead(f.username());
    db.insertDepartmentHeadsForHead(f.department(), f.username());
    return Map.of("status", "ASSIGNED");
  }

  public List<Map<String, Object>> heads() {
    return db.findDepartmentHeadsForHeads();
  }

  public void approvedLeave(long employee, LocalDate from, LocalDate to) {
    db.updateDutyRosterForApprovedLeave(employee, to, from);
    db.updateAppointmentSlotsForApprovedLeave(employee, from, to);
    db.insertRescheduleRequestsForApprovedLeave(employee, from, to);
    notices.department(
        "appointments.manage",
        "Doctor leave affects scheduling",
        "Review the scheduling queue for appointments affected by approved leave");
  }

  public Map<String, String> attendance(StaffingAttendanceRequest f) {
    if (f.employeeId() == null
        || f.workDate() == null
        || !Set.of("PRESENT", "ABSENT", "LEAVE", "HALF_DAY").contains(f.status())
        || f.hoursWorked() == null
        || f.hoursWorked().signum() < 0
        || f.hoursWorked().compareTo(BigDecimal.valueOf(24)) > 0)
      throw bad("Valid attendance and hours required");
    db.insertAttendanceForAttendance(f.employeeId(), f.workDate(), f.status(), f.hoursWorked());
    return Map.of("status", "RECORDED");
  }

  public List<Map<String, Object>> payroll() {
    return db.findSalarySlipsForPayroll();
  }

  public List<Map<String, Object>> attendance() {
    return db.findAttendanceForAttendance();
  }

  public Map<String, String> reconcile(long id, StaffingReconcileRequest f, Principal p) {
    if (f.referenceNo() == null || f.referenceNo().isBlank())
      throw bad("Verified bank reference required");
    if (db.updateSalarySlipsForReconcile(f.referenceNo(), p.getName(), id) == 0)
      throw conflict("Payslip is not pending");
    return Map.of("status", "PAID");
  }

  private LocalDate date(Object o) {
    return o instanceof LocalDate d ? d : ((java.sql.Date) o).toLocalDate();
  }

  private LocalTime time(Object o) {
    return o instanceof LocalTime t ? t : ((java.sql.Time) o).toLocalTime();
  }

  private ResponseStatusException bad(String s) {
    return new ResponseStatusException(HttpStatus.BAD_REQUEST, s);
  }

  private ResponseStatusException conflict(String s) {
    return new ResponseStatusException(HttpStatus.CONFLICT, s);
  }

  private ResponseStatusException forbidden() {
    return new ResponseStatusException(HttpStatus.FORBIDDEN, "Department head approval required");
  }
}
