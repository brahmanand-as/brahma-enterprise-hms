package com.brahma.arogya.hr.controller;

import com.brahma.arogya.hr.dto.StaffPortalLeaveFormRequest;
import com.brahma.arogya.hr.dto.StaffPortalNewStaffRequest;
import com.brahma.arogya.hr.dto.StaffPortalReviewRequest;
import com.brahma.arogya.hr.dto.StaffPortalSalaryFormRequest;
import com.brahma.arogya.hr.service.StaffPortalService;
import java.util.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class StaffPortalController {

  private final StaffPortalService service;

  public StaffPortalController(StaffPortalService service) {
    this.service = service;
  }

  @PostMapping("/staff")
  public Map<String, Object> create(
      @RequestBody @jakarta.validation.Valid() StaffPortalNewStaffRequest req) {
    return service.create(req);
  }

  @GetMapping("/staff")
  public List<Map<String, Object>> staff() {
    return service.staff();
  }

  @GetMapping("/hr/me/profile")
  public Map<String, Object> profile(Authentication auth) {
    return service.profile(auth);
  }

  @GetMapping("/hr/me/salary")
  public List<Map<String, Object>> salary(Authentication auth) {
    return service.salary(auth);
  }

  @GetMapping("/hr/me/leaves")
  public List<Map<String, Object>> leaves(Authentication auth) {
    return service.leaves(auth);
  }

  @PostMapping("/hr/me/leaves")
  public Map<String, String> apply(
      @RequestBody @jakarta.validation.Valid() StaffPortalLeaveFormRequest f, Authentication auth) {
    return service.apply(f, auth);
  }

  @GetMapping("/hr/admin/leaves")
  public List<Map<String, Object>> allLeaves(Authentication auth) {
    return service.allLeaves(auth);
  }

  @PostMapping("/hr/admin/leaves/{id}/review")
  public Map<String, String> review(
      @PathVariable Long id,
      @RequestBody @jakarta.validation.Valid() StaffPortalReviewRequest req,
      Authentication auth) {
    return service.review(id, req, auth);
  }

  @PostMapping("/hr/admin/salary")
  public Map<String, String> salaryEntry(
      @RequestBody @jakarta.validation.Valid() StaffPortalSalaryFormRequest f) {
    return service.salaryEntry(f);
  }
}
