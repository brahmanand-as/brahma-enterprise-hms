package com.brahma.arogya.inventory.repository;

import com.brahma.arogya.inventory.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {}
