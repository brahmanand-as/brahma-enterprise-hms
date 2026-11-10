package com.brahma.arogya.laboratory.service;

import com.brahma.arogya.laboratory.entity.LabOrder;
import com.brahma.arogya.laboratory.repository.LabOrderRepository;
import java.util.*;

@org.springframework.stereotype.Service()
public class LabOrderService {

  private final LabOrderRepository r;

  public LabOrderService(LabOrderRepository r) {
    this.r = r;
  }

  public List<LabOrder> all() {
    return r.findAll();
  }

  public LabOrder one(Long id) {
    return r.findById(id).orElseThrow();
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  public LabOrder create(LabOrder x) {
    return r.save(x);
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  public LabOrder update(Long id, LabOrder x) {
    x.setId(id);
    return r.save(x);
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  public void delete(Long id) {
    r.deleteById(id);
  }
}
