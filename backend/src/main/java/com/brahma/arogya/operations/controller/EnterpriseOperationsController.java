package com.brahma.arogya.operations.controller;

import com.brahma.arogya.operations.dto.EnterpriseOperationsClaimTransitionRequest;
import com.brahma.arogya.operations.dto.EnterpriseOperationsRadiologyTransitionRequest;
import com.brahma.arogya.operations.dto.EnterpriseOperationsShiftRequest;
import com.brahma.arogya.operations.service.EnterpriseOperationsService;
import java.security.Principal;
import java.time.*;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/enterprise")
public class EnterpriseOperationsController {

  private final EnterpriseOperationsService service;

  public EnterpriseOperationsController(EnterpriseOperationsService service) {
    this.service = service;
  }

  @GetMapping("/roster")
  public List<Map<String, Object>> roster() {
    return service.roster();
  }

  @PostMapping("/roster")
  public Map<String, String> roster(
      @RequestBody @jakarta.validation.Valid() EnterpriseOperationsShiftRequest r) {
    return service.roster(r);
  }

  @PostMapping("/roster/{id}/absent")
  public Map<String, String> absent(@PathVariable long id, Principal p) {
    return service.absent(id, p);
  }

  @GetMapping("/insurance")
  public List<Map<String, Object>> claims() {
    return service.claims();
  }

  @PostMapping("/insurance/{id}/transition")
  public Map<String, Object> claim(
      @PathVariable long id,
      @RequestBody @jakarta.validation.Valid() EnterpriseOperationsClaimTransitionRequest r,
      Principal p) {
    return service.claim(id, r, p);
  }

  @PostMapping("/radiology/{id}/transition")
  public Map<String, String> radiology(
      @PathVariable long id,
      @RequestBody @jakarta.validation.Valid() EnterpriseOperationsRadiologyTransitionRequest r,
      Principal p) {
    return service.radiology(id, r, p);
  }
}
