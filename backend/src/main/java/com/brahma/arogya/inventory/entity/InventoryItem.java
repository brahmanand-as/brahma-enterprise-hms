package com.brahma.arogya.inventory.entity;

import jakarta.persistence.*;
import java.math.*;
import java.time.*;
import lombok.*;

@Entity
@Table(name = "inventory_items")
@Getter
@Setter
@NoArgsConstructor
public class InventoryItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String sku;

  private String name;

  private String category;

  private BigDecimal stockQty;

  private BigDecimal reorderLevel;

  private String unit;
}
