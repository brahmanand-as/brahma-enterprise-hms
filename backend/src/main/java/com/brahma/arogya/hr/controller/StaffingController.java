package com.brahma.arogya.hr.controller;

import com.brahma.arogya.hr.dto.StaffingAttendanceRequest;
import com.brahma.arogya.hr.dto.StaffingHeadRequest;
import com.brahma.arogya.hr.dto.StaffingReconcileRequest;
import com.brahma.arogya.hr.dto.StaffingReviewRequest;
import com.brahma.arogya.hr.dto.StaffingShiftRequest;
import com.brahma.arogya.hr.dto.StaffingSwapRequest;
import com.brahma.arogya.hr.service.StaffingService;
import java.math.*;
import java.security.Principal;
import java.time.*;
import java.util.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/staffing")
public class StaffingController {

  private final StaffingService service;

  public StaffingController(StaffingService service) {
    this.service = service;
  }

  @GetMapping("/roster")
  public List<Map<String, Object>> roster() {
    return service.roster();
  }

  @PostMapping("/roster")
  public Map<String, String> shift(
      @RequestBody @jakarta.validation.Valid() StaffingShiftRequest f) {
    return service.shift(f);
  }

  @GetMapping("/requests")
  public List<Map<String, Object>> requests(Principal p, Authentication auth) {
    return service.requests(p, auth);
  }

  @PostMapping("/requests")
  public Map<String, String> swap(
      @RequestBody @jakarta.validation.Valid() StaffingSwapRequest f,
      Principal p,
      Authentication auth) {
    return service.swap(f, p, auth);
  }

  @PostMapping("/requests/{id}/review")
  public Map<String, String> review(
      @PathVariable long id,
      @RequestBody @jakarta.validation.Valid() StaffingReviewRequest f,
      Principal p,
      Authentication auth) {
    return service.review(id, f, p, auth);
  }

  @PostMapping("/heads")
  public Map<String, String> head(@RequestBody @jakarta.validation.Valid() StaffingHeadRequest f) {
    return service.head(f);
  }

  @GetMapping("/heads")
  public List<Map<String, Object>> heads() {
    return service.heads();
  }

  @PostMapping("/attendance")
  public Map<String, String> attendance(
      @RequestBody @jakarta.validation.Valid() StaffingAttendanceRequest f) {
    return service.attendance(f);
  }

  @GetMapping("/payroll")
  public List<Map<String, Object>> payroll() {
    return service.payroll();
  }

  @GetMapping("/attendance")
  public List<Map<String, Object>> attendance() {
    return service.attendance();
  }

  @PostMapping("/salary/{id}/reconcile")
  public Map<String, String> reconcile(
      @PathVariable long id,
      @RequestBody @jakarta.validation.Valid() StaffingReconcileRequest f,
      Principal p) {
    return service.reconcile(id, f, p);
  }
}
