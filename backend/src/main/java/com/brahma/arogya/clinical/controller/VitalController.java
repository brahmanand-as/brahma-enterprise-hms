package com.brahma.arogya.clinical.controller;

import com.brahma.arogya.clinical.dto.VitalRequest;
import com.brahma.arogya.clinical.dto.VitalResponse;
import com.brahma.arogya.clinical.mapper.VitalMapper;
import com.brahma.arogya.clinical.service.VitalService;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vitals")
public class VitalController {

  private final VitalService service;

  public VitalController(VitalService service) {
    this.service = service;
  }

  @GetMapping
  public List<VitalResponse> all() {
    return service.all().stream().map(VitalMapper::toResponse).toList();
  }

  @GetMapping("/{id}")
  public VitalResponse one(@PathVariable Long id) {
    return VitalMapper.toResponse(service.one(id));
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  @PostMapping
  public VitalResponse create(@RequestBody @jakarta.validation.Valid() VitalRequest x) {
    return VitalMapper.toResponse(service.create(VitalMapper.toEntity(x)));
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  @PutMapping("/{id}")
  public VitalResponse update(
      @PathVariable Long id, @RequestBody @jakarta.validation.Valid() VitalRequest x) {
    return VitalMapper.toResponse(service.update(id, VitalMapper.toEntity(x)));
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }
}
