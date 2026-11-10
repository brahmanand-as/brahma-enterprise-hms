package com.brahma.arogya.operations.service;

import com.brahma.arogya.billing.service.RevenueLedger;
import com.brahma.arogya.document.dto.ClinicalDocumentIssueRequest;
import com.brahma.arogya.document.service.ClinicalDocumentService;
import com.brahma.arogya.hr.dto.StaffingShiftRequest;
import com.brahma.arogya.hr.service.StaffingService;
import com.brahma.arogya.operations.dto.EnterpriseOperationsClaimTransitionRequest;
import com.brahma.arogya.operations.dto.EnterpriseOperationsRadiologyTransitionRequest;
import com.brahma.arogya.operations.dto.EnterpriseOperationsShiftRequest;
import com.brahma.arogya.operations.repository.EnterpriseOperationsJdbcRepository;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@org.springframework.stereotype.Service()
public class EnterpriseOperationsService {

  private final ClinicalDocumentService documents;

  private final StaffingService staffing;

  private final RevenueLedger revenue;

  private final EnterpriseOperationsJdbcRepository db;

  public EnterpriseOperationsService(
      EnterpriseOperationsJdbcRepository db,
      ClinicalDocumentService documents,
      StaffingService staffing,
      RevenueLedger revenue) {
    this.db = db;
    this.documents = documents;
    this.staffing = staffing;
    this.revenue = revenue;
  }

  private ResponseStatusException bad(String s) {
    return new ResponseStatusException(HttpStatus.BAD_REQUEST, s);
  }

  public List<Map<String, Object>> roster() {
    return db.findDutyRosterForRoster();
  }

  public Map<String, String> roster(EnterpriseOperationsShiftRequest r) {
    return staffing.shift(
        new StaffingShiftRequest(r.employeeId(), r.shiftDate(), r.startsAt(), r.endsAt(), null));
  }

  @Transactional
  public Map<String, String> absent(long id, Principal p) {
    var r = db.findDutyRosterForAbsent(id);
    db.updateDutyRosterForAbsent(id);
    db.updateAppointmentSlotsForAbsent(r.get("employee_id"), r.get("shift_date"));
    db.insertRescheduleRequestsForAbsent(p.getName(), r.get("employee_id"), r.get("shift_date"));
    return Map.of("status", "ABSENT");
  }

  public List<Map<String, Object>> claims() {
    return db.findInsuranceClaimsForClaims();
  }

  @Transactional
  public Map<String, Object> claim(
      long id, EnterpriseOperationsClaimTransitionRequest r, Principal p) {
    var c = db.findInsuranceClaimsForClaim(id);
    String old = String.valueOf(c.get("status"));
    Map<String, Set<String>> steps =
        Map.of(
            "DRAFT",
            Set.of("PREAUTH_SUBMITTED"),
            "PREAUTH_SUBMITTED",
            Set.of("APPROVED", "REJECTED"),
            "APPROVED",
            Set.of("ENHANCEMENT_REQUESTED", "FINAL_SUBMITTED"),
            "ENHANCEMENT_REQUESTED",
            Set.of("APPROVED", "REJECTED"),
            "FINAL_SUBMITTED",
            Set.of("SETTLED", "REJECTED"));
    if (!steps.getOrDefault(old, Set.of()).contains(r.status()))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Invalid claim transition");
    if (r.amount() == null || r.amount().signum() < 0) throw bad("Nonnegative amount required");
    if ("SETTLED".equals(r.status())
        || "ENHANCEMENT_REQUESTED".equals(r.status())
        || "ENHANCEMENT_REQUESTED".equals(old))
      throw bad("Use enhancement review and settlement reconciliation workflows");
    if ("APPROVED".equals(r.status())
        && r.amount()
                .compareTo(
                    ((BigDecimal) c.get("claim_amount"))
                        .subtract((BigDecimal) c.get("non_payable")))
            > 0) throw bad("Authorization exceeds eligible amount");
    db.updateInsuranceClaimsForClaim(
        r.status(), r.status(), r.amount(), r.status(), r.amount(), id);
    if ("REJECTED".equals(r.status())) {
      var allocations = db.findInsuranceInvoiceAllocationsForClaim(id);
      db.updateInsuranceInvoiceAllocationsForClaim(id);
      db.updateInsuranceClaimsForClaim2(id);
      for (Long invoice : allocations) revenue.status(invoice);
    }
    db.insertInsuranceEventsForClaim(id, r.status(), r.amount(), r.notes(), p.getName());
    return db.findInsuranceClaimsForClaim2(id);
  }

  @Transactional
  public Map<String, String> radiology(
      long id, EnterpriseOperationsRadiologyTransitionRequest r, Principal p) {
    var row = db.findRadiologyOrdersForRadiology(id);
    String expected =
        Map.of(
                "ORDERED",
                "SCHEDULED",
                "SCHEDULED",
                "IN_PROCESS",
                "IN_PROCESS",
                "REPORTED",
                "REPORTED",
                "VERIFIED")
            .get(String.valueOf(row.get("status")));
    if (!Objects.equals(expected, r.status()))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Invalid radiology transition");
    if (Set.of("REPORTED", "VERIFIED").contains(r.status())
        && (r.report() == null || r.report().isBlank())) throw bad("Report text required");
    db.updateRadiologyOrdersForRadiology(
        r.status(), r.report(), r.status(), p.getName(), r.status(), id);
    if ("VERIFIED".equals(r.status()))
      documents.issue(new ClinicalDocumentIssueRequest("RADIOLOGY_REPORT", id), p);
    return Map.of("status", r.status());
  }
}
