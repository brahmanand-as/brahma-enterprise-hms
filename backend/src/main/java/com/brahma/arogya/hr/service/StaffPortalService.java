package com.brahma.arogya.hr.service;

import com.brahma.arogya.hr.dto.StaffPortalLeaveFormRequest;
import com.brahma.arogya.hr.dto.StaffPortalNewStaffRequest;
import com.brahma.arogya.hr.dto.StaffPortalReviewRequest;
import com.brahma.arogya.hr.dto.StaffPortalSalaryFormRequest;
import com.brahma.arogya.hr.repository.StaffPortalJdbcRepository;
import com.brahma.arogya.notification.service.CoordinationService;
import java.util.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@org.springframework.stereotype.Service()
public class StaffPortalService {

  private final StaffingService staffing;

  private final CoordinationService notices;

  private final StaffPortalJdbcRepository db;

  private final PasswordEncoder encoder;

  public StaffPortalService(
      StaffPortalJdbcRepository db,
      PasswordEncoder encoder,
      StaffingService staffing,
      CoordinationService notices) {
    this.db = db;
    this.encoder = encoder;
    this.staffing = staffing;
    this.notices = notices;
  }

  private static final Set<String> ROLES =
      Set.of(
          "ADMIN",
          "HR",
          "DOCTOR",
          "NURSE",
          "RECEPTIONIST",
          "LAB_TECHNICIAN",
          "LAB_SUPERVISOR",
          "PATHOLOGIST",
          "PHARMACIST",
          "BILLING",
          "IPD",
          "RADIOLOGIST",
          "HELPDESK",
          "INSURANCE");

  @Transactional
  public Map<String, Object> create(StaffPortalNewStaffRequest req) {
    if ("ADMIN".equals(req.role())
        && !org.springframework.security.core.context.SecurityContextHolder.getContext()
            .getAuthentication()
            .getAuthorities()
            .stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")))
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Only administrators can create administrators");
    if (req.username() == null
        || !req.username().matches("[A-Za-z0-9._-]{3,80}")
        || req.password() == null
        || req.password().length() < 12
        || db.countEnterpriseRolesForCreate(req.role()) == 0)
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Invalid username, password or role");
    if (req.employeeId() != null) {
      Integer exists = db.countEmployeesForCreate(req.employeeId());
      if (exists == null || exists == 0)
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Employee does not exist. Select an employee from the list.");
    }
    db.insertStaffAccountsForCreate(
        req.username(), encoder.encode(req.password()), req.role(), req.employeeId());
    return Map.of("username", req.username(), "role", req.role());
  }

  public List<Map<String, Object>> staff() {
    return db.findStaffAccountsForStaff();
  }

  private long employeeId(Authentication auth) {
    List<Long> ids = db.findStaffAccountsForEmployeeId(auth.getName());
    if (ids.isEmpty() || ids.get(0) == null)
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Staff account not linked to an employee");
    return ids.get(0);
  }

  public Map<String, Object> profile(Authentication auth) {
    List<Map<String, Object>> rows = db.findEmployeesForProfile(employeeId(auth));
    if (rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    return rows.get(0);
  }

  public List<Map<String, Object>> salary(Authentication auth) {
    return db.findSalarySlipsForSalary(employeeId(auth));
  }

  public List<Map<String, Object>> leaves(Authentication auth) {
    return db.findLeaveRequestsForLeaves(employeeId(auth));
  }

  @Transactional
  public Map<String, String> apply(StaffPortalLeaveFormRequest f, Authentication auth) {
    if (f.leaveType() == null || f.startDate() == null || f.endDate() == null)
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Leave type and dates required");
    java.time.LocalDate start, end;
    try {
      start = java.time.LocalDate.parse(f.startDate());
      end = java.time.LocalDate.parse(f.endDate());
    } catch (Exception ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid dates");
    }
    if (end.isBefore(start))
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date precedes start date");
    db.insertLeaveRequestsForApply(employeeId(auth), f.leaveType(), start, end, f.reason());
    var heads = db.findDepartmentHeadsForApply(employeeId(auth));
    if (heads.isEmpty())
      notices.department(
          "hr.manage", "Leave approval requested", "An employee leave request awaits review");
    else
      for (String head : heads)
        notices.notify(
            head, "Leave approval requested", "A leave request in your department awaits review");
    return Map.of("status", "PENDING");
  }

  public List<Map<String, Object>> allLeaves(Authentication auth) {
    boolean hr =
        auth.getAuthorities().stream()
            .anyMatch(a -> Set.of("ROLE_ADMIN", "PERM_hr.manage").contains(a.getAuthority()));
    return hr
        ? db.findLeaveRequestsForAllLeaves()
        : db.findLeaveRequestsForAllLeaves2(auth.getName());
  }

  @Transactional
  public Map<String, String> review(Long id, StaffPortalReviewRequest req, Authentication auth) {
    if (!Set.of("APPROVED", "REJECTED").contains(req.status()))
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid decision");
    staffing.head(db.readLeaveRequestsForReview(id), auth, auth);
    int n = db.updateLeaveRequestsForReview(req.status(), auth.getName(), id);
    if (n == 0) throw new ResponseStatusException(HttpStatus.CONFLICT, "Leave not pending");
    if ("APPROVED".equals(req.status())) {
      var leave = db.findLeaveRequestsForReview(id);
      Object start = leave.get("start_date"), end = leave.get("end_date");
      staffing.approvedLeave(
          ((Number) leave.get("employee_id")).longValue(),
          start instanceof java.time.LocalDate d ? d : ((java.sql.Date) start).toLocalDate(),
          end instanceof java.time.LocalDate d ? d : ((java.sql.Date) end).toLocalDate());
    }
    return Map.of("status", req.status());
  }

  @Transactional
  public Map<String, String> salaryEntry(StaffPortalSalaryFormRequest f) {
    if (f.employeeId() == null
        || f.salaryMonth() == null
        || !f.salaryMonth().matches("\\d{4}-(0[1-9]|1[0-2])")
        || f.basic() == null
        || f.basic().signum() < 0)
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid salary details");
    java.math.BigDecimal
        allowances = f.allowances() == null ? java.math.BigDecimal.ZERO : f.allowances(),
        deductions = f.deductions() == null ? java.math.BigDecimal.ZERO : f.deductions();
    if (allowances.signum() < 0 || deductions.signum() < 0)
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Allowances and deductions cannot be negative");
    java.math.BigDecimal net = f.basic().add(allowances).subtract(deductions);
    if (net.signum() < 0)
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Net salary cannot be negative");
    // Payroll payment status must only be updated after bank reconciliation.
    String // Payroll payment status must only be updated after bank reconciliation.
        status = "PENDING";
    db.insertSalarySlipsForSalaryEntry(
        f.employeeId(), f.salaryMonth(), f.basic(), allowances, deductions, net, status);
    return Map.of("status", status, "netPay", net.toPlainString());
  }
}
