package com.brahma.arogya.clinical.controller;

import com.brahma.arogya.clinical.dto.EncounterRequest;
import com.brahma.arogya.clinical.dto.EncounterResponse;
import com.brahma.arogya.clinical.mapper.EncounterMapper;
import com.brahma.arogya.clinical.service.EncounterService;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/encounters")
public class EncounterController {

  private final EncounterService service;

  public EncounterController(EncounterService service) {
    this.service = service;
  }

  @GetMapping
  public List<EncounterResponse> all() {
    return service.all().stream().map(EncounterMapper::toResponse).toList();
  }

  @GetMapping("/{id}")
  public EncounterResponse one(@PathVariable Long id) {
    return EncounterMapper.toResponse(service.one(id));
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  @PostMapping
  public EncounterResponse create(@RequestBody @jakarta.validation.Valid() EncounterRequest x) {
    return EncounterMapper.toResponse(service.create(EncounterMapper.toEntity(x)));
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  @PutMapping("/{id}")
  public EncounterResponse update(
      @PathVariable Long id, @RequestBody @jakarta.validation.Valid() EncounterRequest x) {
    return EncounterMapper.toResponse(service.update(id, EncounterMapper.toEntity(x)));
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }
}
