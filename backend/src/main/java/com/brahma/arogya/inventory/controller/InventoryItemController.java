package com.brahma.arogya.inventory.controller;

import com.brahma.arogya.inventory.dto.InventoryItemRequest;
import com.brahma.arogya.inventory.dto.InventoryItemResponse;
import com.brahma.arogya.inventory.mapper.InventoryItemMapper;
import com.brahma.arogya.inventory.service.InventoryItemService;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
public class InventoryItemController {

  private final InventoryItemService service;

  public InventoryItemController(InventoryItemService service) {
    this.service = service;
  }

  @GetMapping
  public List<InventoryItemResponse> all() {
    return service.all().stream().map(InventoryItemMapper::toResponse).toList();
  }

  @GetMapping("/{id}")
  public InventoryItemResponse one(@PathVariable Long id) {
    return InventoryItemMapper.toResponse(service.one(id));
  }

  @PostMapping
  public InventoryItemResponse create(
      @RequestBody @jakarta.validation.Valid() InventoryItemRequest x) {
    return InventoryItemMapper.toResponse(service.create(InventoryItemMapper.toEntity(x)));
  }

  @PutMapping("/{id}")
  public InventoryItemResponse update(
      @PathVariable Long id, @RequestBody @jakarta.validation.Valid() InventoryItemRequest x) {
    return InventoryItemMapper.toResponse(service.update(id, InventoryItemMapper.toEntity(x)));
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }
}
