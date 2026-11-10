package com.brahma.arogya.inventory.service;

import com.brahma.arogya.inventory.entity.InventoryItem;
import com.brahma.arogya.inventory.repository.InventoryItemRepository;
import java.util.*;

@org.springframework.stereotype.Service()
public class InventoryItemService {

  private final InventoryItemRepository r;

  public InventoryItemService(InventoryItemRepository r) {
    this.r = r;
  }

  public List<InventoryItem> all() {
    return r.findAll();
  }

  public InventoryItem one(Long id) {
    return r.findById(id).orElseThrow();
  }

  public InventoryItem create(InventoryItem x) {
    if (x.getId() != null
        || x.getName() == null
        || x.getName().isBlank()
        || x.getSku() == null
        || x.getSku().isBlank())
      throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.BAD_REQUEST,
          "Unique stock code and item name required");
    x.setStockQty(java.math.BigDecimal.ZERO);
    return r.save(x);
  }

  public InventoryItem update(Long id, InventoryItem x) {
    InventoryItem old = one(id);
    x.setId(id);
    x.setStockQty(old.getStockQty());
    return r.save(x);
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  public void delete(Long id) {
    r.deleteById(id);
  }
}
