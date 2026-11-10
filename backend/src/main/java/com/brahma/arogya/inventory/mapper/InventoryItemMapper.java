package com.brahma.arogya.inventory.mapper;

import com.brahma.arogya.inventory.dto.InventoryItemRequest;
import com.brahma.arogya.inventory.dto.InventoryItemResponse;
import com.brahma.arogya.inventory.entity.InventoryItem;

public final class InventoryItemMapper {

  public static InventoryItem toEntity(InventoryItemRequest value) {
    if (value == null) return null;
    var result = new InventoryItem();
    result.setId(value.getId());
    result.setSku(value.getSku());
    result.setName(value.getName());
    result.setCategory(value.getCategory());
    result.setStockQty(value.getStockQty());
    result.setReorderLevel(value.getReorderLevel());
    result.setUnit(value.getUnit());
    return result;
  }

  public static InventoryItemResponse toResponse(InventoryItem value) {
    if (value == null) return null;
    var result = new InventoryItemResponse();
    result.setId(value.getId());
    result.setSku(value.getSku());
    result.setName(value.getName());
    result.setCategory(value.getCategory());
    result.setStockQty(value.getStockQty());
    result.setReorderLevel(value.getReorderLevel());
    result.setUnit(value.getUnit());
    return result;
  }
}
