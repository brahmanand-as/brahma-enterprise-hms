package com.brahma.arogya.inpatient.controller;

import com.brahma.arogya.inpatient.dto.InpatientBedRequest;
import com.brahma.arogya.inpatient.dto.InpatientBedStatusRequest;
import com.brahma.arogya.inpatient.dto.InpatientLinkRequest;
import com.brahma.arogya.inpatient.dto.InpatientMedicationRequest;
import com.brahma.arogya.inpatient.dto.InpatientNoteRequest;
import com.brahma.arogya.inpatient.dto.InpatientRecommendationRequest;
import com.brahma.arogya.inpatient.service.InpatientService;
import java.math.*;
import java.security.Principal;
import java.time.*;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inpatient")
public class InpatientController {

  private final InpatientService service;

  public InpatientController(InpatientService service) {
    this.service = service;
  }

  @GetMapping("/beds")
  public List<Map<String, Object>> beds() {
    return service.beds();
  }

  @PostMapping("/beds")
  public Map<String, String> bed(@RequestBody @jakarta.validation.Valid() InpatientBedRequest b) {
    return service.bed(b);
  }

  @PutMapping("/beds/{id}")
  public Map<String, String> updateBed(
      @PathVariable long id, @RequestBody @jakarta.validation.Valid() InpatientBedRequest f) {
    return service.updateBed(id, f);
  }

  @PostMapping("/beds/{id}/status")
  public Map<String, String> bedState(
      @PathVariable long id,
      @RequestBody @jakarta.validation.Valid() InpatientBedStatusRequest f,
      Principal p) {
    return service.bedState(id, f, p);
  }

  @GetMapping("/recommendations")
  public List<Map<String, Object>> recommendations() {
    return service.recommendations();
  }

  @PostMapping("/recommendations")
  public Map<String, String> recommend(
      @RequestBody @jakarta.validation.Valid() InpatientRecommendationRequest f, Principal p) {
    return service.recommend(f, p);
  }

  @PostMapping("/recommendations/{id}/allocate")
  public Map<String, String> allocate(
      @PathVariable long id, @RequestBody @jakarta.validation.Valid() InpatientLinkRequest f) {
    return service.allocate(id, f);
  }

  @GetMapping("/medication-orders")
  public List<Map<String, Object>> medications() {
    return service.medications();
  }

  @PostMapping("/medication-orders")
  public Map<String, Object> order(
      @RequestBody @jakarta.validation.Valid() InpatientMedicationRequest f, Principal p) {
    return service.order(f, p);
  }

  @PostMapping("/medication-orders/{id}/verify")
  public Map<String, String> verify(@PathVariable long id, Principal p) {
    return service.verify(id, p);
  }

  @GetMapping("/nursing-notes")
  public List<Map<String, Object>> notes() {
    return service.notes();
  }

  @PostMapping("/nursing-notes")
  public Map<String, String> note(
      @RequestBody @jakarta.validation.Valid() InpatientNoteRequest f, Principal p) {
    return service.note(f, p);
  }
}
