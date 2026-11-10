package com.brahma.arogya.inpatient.controller;

import com.brahma.arogya.inpatient.dto.WardAdministrationRequest;
import com.brahma.arogya.inpatient.dto.WardMedicationRequest;
import com.brahma.arogya.inpatient.dto.WardTransferRequest;
import com.brahma.arogya.inpatient.service.WardService;
import java.security.Principal;
import java.time.*;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ward")
public class WardController {

  private final WardService service;

  public WardController(WardService service) {
    this.service = service;
  }

  @GetMapping("/medications")
  public List<Map<String, Object>> medications() {
    return service.medications();
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  @PostMapping("/medications")
  public Map<String, String> order(
      @RequestBody @jakarta.validation.Valid() WardMedicationRequest r, Principal p) {
    return service.order(r, p);
  }

  @PostMapping("/medications/{id}/administer")
  public Map<String, String> administer(
      @PathVariable long id,
      @RequestBody @jakarta.validation.Valid() WardAdministrationRequest r,
      Principal p) {
    return service.administer(id, r, p);
  }

  @PostMapping("/admissions/{id}/transfer")
  public Map<String, String> transfer(
      @PathVariable long id,
      @RequestBody @jakarta.validation.Valid() WardTransferRequest r,
      Principal p) {
    return service.transfer(id, r, p);
  }

  @PostMapping("/beds/{id}/ready")
  public Map<String, String> ready(@PathVariable long id) {
    return service.ready(id);
  }
}
