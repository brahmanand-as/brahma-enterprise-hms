package com.brahma.arogya.laboratory.controller;

import com.brahma.arogya.laboratory.dto.LabOrderItemRequest;
import com.brahma.arogya.laboratory.dto.LabOrderItemResponse;
import com.brahma.arogya.laboratory.mapper.LabOrderItemMapper;
import com.brahma.arogya.laboratory.service.LabOrderItemService;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lab-order-items")
public class LabOrderItemController {

  private final LabOrderItemService service;

  public LabOrderItemController(LabOrderItemService service) {
    this.service = service;
  }

  @GetMapping
  public List<LabOrderItemResponse> all() {
    return service.all().stream().map(LabOrderItemMapper::toResponse).toList();
  }

  @GetMapping("/{id}")
  public LabOrderItemResponse one(@PathVariable Long id) {
    return LabOrderItemMapper.toResponse(service.one(id));
  }

  @PostMapping
  public LabOrderItemResponse create(
      @RequestBody @jakarta.validation.Valid() LabOrderItemRequest x) {
    return LabOrderItemMapper.toResponse(service.create(LabOrderItemMapper.toEntity(x)));
  }

  @PutMapping("/{id}")
  public LabOrderItemResponse update(
      @PathVariable Long id, @RequestBody @jakarta.validation.Valid() LabOrderItemRequest x) {
    return LabOrderItemMapper.toResponse(service.update(id, LabOrderItemMapper.toEntity(x)));
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }
}
