package com.brahma.arogya.clinical.service;

import com.brahma.arogya.clinical.entity.Vital;
import com.brahma.arogya.clinical.repository.VitalRepository;
import java.util.*;

@org.springframework.stereotype.Service()
public class VitalService {

  private final VitalRepository r;

  public VitalService(VitalRepository r) {
    this.r = r;
  }

  public List<Vital> all() {
    return r.findAll();
  }

  public Vital one(Long id) {
    return r.findById(id).orElseThrow();
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  public Vital create(Vital x) {
    return r.save(x);
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  public Vital update(Long id, Vital x) {
    x.setId(id);
    return r.save(x);
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  public void delete(Long id) {
    r.deleteById(id);
  }
}
