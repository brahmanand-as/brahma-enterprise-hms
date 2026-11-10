package com.brahma.arogya.portal.controller;

import com.brahma.arogya.portal.dto.EnterprisePortalProfileRequest;
import com.brahma.arogya.portal.dto.EnterprisePortalRegistrationRequest;
import com.brahma.arogya.portal.dto.EnterprisePortalTicketRequest;
import com.brahma.arogya.portal.dto.EnterprisePortalTicketReviewRequest;
import com.brahma.arogya.portal.service.EnterprisePortalService;
import java.security.Principal;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class EnterprisePortalController {

  private final EnterprisePortalService service;

  public EnterprisePortalController(EnterprisePortalService service) {
    this.service = service;
  }

  @PostMapping("/public/register")
  public Map<String, Object> register(
      @RequestBody @jakarta.validation.Valid() EnterprisePortalRegistrationRequest r) {
    return service.register(r);
  }

  @PutMapping("/patient/profile")
  public Map<String, String> updateProfile(
      @RequestBody @jakarta.validation.Valid() EnterprisePortalProfileRequest f, Principal p) {
    return service.updateProfile(f, p);
  }

  @GetMapping("/patient/profile")
  public Map<String, Object> profile(Principal p) {
    return service.profile(p);
  }

  @GetMapping("/patient/appointments")
  public List<Map<String, Object>> appointments(Principal p) {
    return service.appointments(p);
  }

  @GetMapping("/patient/invoices")
  public List<Map<String, Object>> invoices(Principal p) {
    return service.invoices(p);
  }

  @GetMapping("/patient/prescriptions")
  public List<Map<String, Object>> prescriptions(Principal p) {
    return service.prescriptions(p);
  }

  @GetMapping("/patient/reports")
  public List<Map<String, Object>> reports(Principal p) {
    return service.reports(p);
  }

  @PostMapping("/patient/registration-fee-demo")
  public Map<String, String> fee(Principal p) {
    return service.fee(p);
  }

  @GetMapping("/helpdesk/tickets")
  public List<Map<String, Object>> tickets(
      Principal p, org.springframework.security.core.Authentication auth) {
    return service.tickets(p, auth);
  }

  @PostMapping("/helpdesk/tickets")
  public Map<String, String> ticket(
      @RequestBody @jakarta.validation.Valid() EnterprisePortalTicketRequest r, Principal p) {
    return service.ticket(r, p);
  }

  @PostMapping("/helpdesk/tickets/{id}/review")
  public Map<String, String> review(
      @PathVariable long id,
      @RequestBody @jakarta.validation.Valid() EnterprisePortalTicketReviewRequest r) {
    return service.review(id, r);
  }

  @GetMapping("/analytics/monthly")
  public Map<String, Object> analytics(@RequestParam(defaultValue = "2026-10") String month) {
    return service.analytics(month);
  }
}
