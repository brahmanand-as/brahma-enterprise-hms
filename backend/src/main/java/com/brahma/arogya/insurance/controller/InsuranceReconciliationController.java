package com.brahma.arogya.insurance.controller;

import com.brahma.arogya.insurance.dto.InsuranceReconciliationAllocationRequest;
import com.brahma.arogya.insurance.dto.InsuranceReconciliationDecisionRequest;
import com.brahma.arogya.insurance.dto.InsuranceReconciliationEnhancementRequest;
import com.brahma.arogya.insurance.dto.InsuranceReconciliationLinesRequest;
import com.brahma.arogya.insurance.dto.InsuranceReconciliationSettlementRequest;
import com.brahma.arogya.insurance.service.InsuranceReconciliationService;
import java.math.*;
import java.security.Principal;
import java.time.*;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coverage")
public class InsuranceReconciliationController {

  private final InsuranceReconciliationService service;

  public InsuranceReconciliationController(InsuranceReconciliationService service) {
    this.service = service;
  }

  @GetMapping("/enhancements")
  public List<Map<String, Object>> enhancements() {
    return service.enhancements();
  }

  @GetMapping("/claims")
  public List<Map<String, Object>> list() {
    return service.list();
  }

  @GetMapping("/claims/{id}")
  public Map<String, Object> detail(@PathVariable long id) {
    return service.detail(id);
  }

  @PutMapping("/claims/{id}/lines")
  public Map<String, Object> lines(
      @PathVariable long id,
      @RequestBody @jakarta.validation.Valid() InsuranceReconciliationLinesRequest f) {
    return service.lines(id, f);
  }

  @PostMapping("/claims/{id}/enhancements")
  public Map<String, String> enhancement(
      @PathVariable long id,
      @RequestBody @jakarta.validation.Valid() InsuranceReconciliationEnhancementRequest f,
      Principal p) {
    return service.enhancement(id, f, p);
  }

  @PostMapping("/enhancements/{id}/review")
  public Map<String, String> review(
      @PathVariable long id,
      @RequestBody @jakarta.validation.Valid() InsuranceReconciliationDecisionRequest f,
      Principal p) {
    return service.review(id, f, p);
  }

  @PostMapping("/claims/{id}/allocations")
  public Map<String, Object> allocate(
      @PathVariable long id,
      @RequestBody @jakarta.validation.Valid() InsuranceReconciliationAllocationRequest f) {
    return service.allocate(id, f);
  }

  @PostMapping("/claims/{id}/settlements")
  public Map<String, Object> settle(
      @PathVariable long id,
      @RequestBody @jakarta.validation.Valid() InsuranceReconciliationSettlementRequest f,
      Principal p) {
    return service.settle(id, f, p);
  }
}
