package com.brahma.arogya.laboratory.controller;

import com.brahma.arogya.laboratory.dto.LabOrderRequest;
import com.brahma.arogya.laboratory.dto.LabOrderResponse;
import com.brahma.arogya.laboratory.mapper.LabOrderMapper;
import com.brahma.arogya.laboratory.service.LabOrderService;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lab-orders")
public class LabOrderController {

  private final LabOrderService service;

  public LabOrderController(LabOrderService service) {
    this.service = service;
  }

  @GetMapping
  public List<LabOrderResponse> all() {
    return service.all().stream().map(LabOrderMapper::toResponse).toList();
  }

  @GetMapping("/{id}")
  public LabOrderResponse one(@PathVariable Long id) {
    return LabOrderMapper.toResponse(service.one(id));
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  @PostMapping
  public LabOrderResponse create(@RequestBody @jakarta.validation.Valid() LabOrderRequest x) {
    return LabOrderMapper.toResponse(service.create(LabOrderMapper.toEntity(x)));
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  @PutMapping("/{id}")
  public LabOrderResponse update(
      @PathVariable Long id, @RequestBody @jakarta.validation.Valid() LabOrderRequest x) {
    return LabOrderMapper.toResponse(service.update(id, LabOrderMapper.toEntity(x)));
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }
}
