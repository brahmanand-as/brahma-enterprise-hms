package com.brahma.arogya.inventory.dto;

import java.math.*;
import java.time.*;

@lombok.Getter()
@lombok.Setter()
@lombok.NoArgsConstructor()
public class InventoryItemRequest {

  @jakarta.validation.constraints.Positive() private Long id;

  private String sku;

  private String name;

  private String category;

  private BigDecimal stockQty;

  private BigDecimal reorderLevel;

  private String unit;
}
