package com.brahma.arogya.inpatient.controller;

import com.brahma.arogya.inpatient.dto.AdmissionRequest;
import com.brahma.arogya.inpatient.dto.AdmissionResponse;
import com.brahma.arogya.inpatient.mapper.AdmissionMapper;
import com.brahma.arogya.inpatient.service.AdmissionService;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admissions")
public class AdmissionController {

  private final AdmissionService service;

  public AdmissionController(AdmissionService service) {
    this.service = service;
  }

  @GetMapping
  public List<AdmissionResponse> all() {
    return service.all().stream().map(AdmissionMapper::toResponse).toList();
  }

  @GetMapping("/{id}")
  public AdmissionResponse one(@PathVariable Long id) {
    return AdmissionMapper.toResponse(service.one(id));
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  @PostMapping
  public AdmissionResponse create(@RequestBody @jakarta.validation.Valid() AdmissionRequest x) {
    return AdmissionMapper.toResponse(service.create(AdmissionMapper.toEntity(x)));
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  @PutMapping("/{id}")
  public AdmissionResponse update(
      @PathVariable Long id, @RequestBody @jakarta.validation.Valid() AdmissionRequest x) {
    return AdmissionMapper.toResponse(service.update(id, AdmissionMapper.toEntity(x)));
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }
}
