package com.brahma.arogya.appointment.controller;

import com.brahma.arogya.appointment.dto.AppointmentManagementProvisionalRequest;
import com.brahma.arogya.appointment.dto.AppointmentManagementReasonRequest;
import com.brahma.arogya.appointment.dto.AppointmentManagementResolveRequest;
import com.brahma.arogya.appointment.service.AppointmentManagementService;
import java.security.Principal;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AppointmentManagementController {

  private final AppointmentManagementService service;

  public AppointmentManagementController(AppointmentManagementService service) {
    this.service = service;
  }

  @PostMapping({"/scheduling/appointments/{id}/cancel", "/patient/appointments/{id}/cancel"})
  public Map<String, String> cancel(
      @PathVariable long id,
      @RequestBody @jakarta.validation.Valid() AppointmentManagementReasonRequest r,
      Principal p,
      jakarta.servlet.http.HttpServletRequest req) {
    return service.cancel(id, r, p, req);
  }

  @PostMapping("/scheduling/provisional")
  public Map<String, String> provisional(
      @RequestBody @jakarta.validation.Valid() AppointmentManagementProvisionalRequest f,
      Principal p) {
    return service.provisional(f, p);
  }

  @PostMapping("/scheduling/requests/{id}/resolve")
  public Map<String, String> resolve(
      @PathVariable long id,
      @RequestBody @jakarta.validation.Valid() AppointmentManagementResolveRequest f,
      Principal p,
      jakarta.servlet.http.HttpServletRequest req) {
    return service.resolve(id, f, p, req);
  }
}
