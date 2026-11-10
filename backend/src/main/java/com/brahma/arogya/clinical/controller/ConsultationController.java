package com.brahma.arogya.clinical.controller;

import com.brahma.arogya.clinical.dto.ConsultationRequest;
import com.brahma.arogya.clinical.dto.ConsultationResponse;
import com.brahma.arogya.clinical.mapper.ConsultationMapper;
import com.brahma.arogya.clinical.service.ConsultationService;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/consultations")
public class ConsultationController {

  private final ConsultationService service;

  public ConsultationController(ConsultationService service) {
    this.service = service;
  }

  @GetMapping
  public List<ConsultationResponse> all() {
    return service.all().stream().map(ConsultationMapper::toResponse).toList();
  }

  @GetMapping("/{id}")
  public ConsultationResponse one(@PathVariable Long id) {
    return ConsultationMapper.toResponse(service.one(id));
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  @PostMapping
  public ConsultationResponse create(
      @RequestBody @jakarta.validation.Valid() ConsultationRequest x) {
    return ConsultationMapper.toResponse(service.create(ConsultationMapper.toEntity(x)));
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  @PutMapping("/{id}")
  public ConsultationResponse update(
      @PathVariable Long id, @RequestBody @jakarta.validation.Valid() ConsultationRequest x) {
    return ConsultationMapper.toResponse(service.update(id, ConsultationMapper.toEntity(x)));
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }
}
