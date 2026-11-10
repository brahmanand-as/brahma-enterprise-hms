package com.brahma.arogya.patient.controller;

import com.brahma.arogya.patient.dto.PatientRequest;
import com.brahma.arogya.patient.dto.PatientResponse;
import com.brahma.arogya.patient.mapper.PatientMapper;
import com.brahma.arogya.patient.service.PatientService;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

  private final PatientService service;

  public PatientController(PatientService service) {
    this.service = service;
  }

  @GetMapping
  public List<PatientResponse> all() {
    return service.all().stream().map(PatientMapper::toResponse).toList();
  }

  @GetMapping("/{id}")
  public PatientResponse one(@PathVariable Long id) {
    return PatientMapper.toResponse(service.one(id));
  }

  @PostMapping
  public PatientResponse create(@RequestBody @jakarta.validation.Valid() PatientRequest x) {
    return PatientMapper.toResponse(service.create(PatientMapper.toEntity(x)));
  }

  @PutMapping("/{id}")
  public PatientResponse update(
      @PathVariable Long id, @RequestBody @jakarta.validation.Valid() PatientRequest x) {
    return PatientMapper.toResponse(service.update(id, PatientMapper.toEntity(x)));
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }
}
