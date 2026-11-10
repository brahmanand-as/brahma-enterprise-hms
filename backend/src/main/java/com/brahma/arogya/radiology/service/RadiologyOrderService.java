package com.brahma.arogya.radiology.service;

import com.brahma.arogya.radiology.entity.RadiologyOrder;
import com.brahma.arogya.radiology.repository.RadiologyOrderRepository;
import java.util.*;

@org.springframework.stereotype.Service()
public class RadiologyOrderService {

  private final RadiologyOrderRepository r;

  public RadiologyOrderService(RadiologyOrderRepository r) {
    this.r = r;
  }

  public List<RadiologyOrder> all() {
    return r.findAll();
  }

  public RadiologyOrder one(Long id) {
    return r.findById(id).orElseThrow();
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  public RadiologyOrder create(RadiologyOrder x) {
    return r.save(x);
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  public RadiologyOrder update(Long id, RadiologyOrder x) {
    x.setId(id);
    return r.save(x);
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  public void delete(Long id) {
    r.deleteById(id);
  }
}
