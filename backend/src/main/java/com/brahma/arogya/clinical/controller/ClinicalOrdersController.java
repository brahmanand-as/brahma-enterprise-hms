package com.brahma.arogya.clinical.controller;

import com.brahma.arogya.clinical.dto.ClinicalOrdersPrescriptionRequest;
import com.brahma.arogya.clinical.dto.ClinicalOrdersRadiologyRequest;
import com.brahma.arogya.clinical.service.ClinicalOrdersService;
import java.security.Principal;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class ClinicalOrdersController {

  private final ClinicalOrdersService service;

  public ClinicalOrdersController(ClinicalOrdersService service) {
    this.service = service;
  }

  @PostMapping("/radiology")
  public Map<String, String> radiology(
      @RequestBody @jakarta.validation.Valid() ClinicalOrdersRadiologyRequest r) {
    return service.radiology(r);
  }

  @GetMapping("/prescriptions")
  public List<Map<String, Object>> all() {
    return service.all();
  }

  @PostMapping("/prescriptions")
  public Map<String, Object> create(
      @RequestBody @jakarta.validation.Valid() ClinicalOrdersPrescriptionRequest r, Principal p) {
    return service.create(r, p);
  }

  @PostMapping("/prescriptions/{id}/finalize")
  public Map<String, String> finalizeOrder(@PathVariable long id, Principal p) {
    return service.finalizeOrder(id, p);
  }

  @GetMapping("/prescriptions/{id}/items")
  public List<Map<String, Object>> items(@PathVariable long id) {
    return service.items(id);
  }
}
