package com.brahma.arogya.laboratory.service;

import com.brahma.arogya.laboratory.entity.LabOrderItem;
import com.brahma.arogya.laboratory.repository.LabOrderItemRepository;
import java.util.*;

@org.springframework.stereotype.Service()
public class LabOrderItemService {

  private final LabOrderItemRepository r;

  public LabOrderItemService(LabOrderItemRepository r) {
    this.r = r;
  }

  public List<LabOrderItem> all() {
    return r.findAll();
  }

  public LabOrderItem one(Long id) {
    return r.findById(id).orElseThrow();
  }

  public LabOrderItem create(LabOrderItem x) {
    return r.save(x);
  }

  public LabOrderItem update(Long id, LabOrderItem x) {
    x.setId(id);
    return r.save(x);
  }

  public void delete(Long id) {
    r.deleteById(id);
  }
}
