package com.brahma.arogya.radiology.controller;

import com.brahma.arogya.radiology.dto.RadiologyOrderRequest;
import com.brahma.arogya.radiology.dto.RadiologyOrderResponse;
import com.brahma.arogya.radiology.mapper.RadiologyOrderMapper;
import com.brahma.arogya.radiology.service.RadiologyOrderService;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/radiology")
public class RadiologyOrderController {

  private final RadiologyOrderService service;

  public RadiologyOrderController(RadiologyOrderService service) {
    this.service = service;
  }

  @GetMapping
  public List<RadiologyOrderResponse> all() {
    return service.all().stream().map(RadiologyOrderMapper::toResponse).toList();
  }

  @GetMapping("/{id}")
  public RadiologyOrderResponse one(@PathVariable Long id) {
    return RadiologyOrderMapper.toResponse(service.one(id));
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  @PostMapping
  public RadiologyOrderResponse create(
      @RequestBody @jakarta.validation.Valid() RadiologyOrderRequest x) {
    return RadiologyOrderMapper.toResponse(service.create(RadiologyOrderMapper.toEntity(x)));
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  @PutMapping("/{id}")
  public RadiologyOrderResponse update(
      @PathVariable Long id, @RequestBody @jakarta.validation.Valid() RadiologyOrderRequest x) {
    return RadiologyOrderMapper.toResponse(service.update(id, RadiologyOrderMapper.toEntity(x)));
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }
}
