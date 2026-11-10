package com.brahma.arogya.inpatient.service;

import com.brahma.arogya.inpatient.entity.Admission;
import com.brahma.arogya.inpatient.repository.AdmissionRepository;
import java.util.*;

@org.springframework.stereotype.Service()
public class AdmissionService {

  private final AdmissionRepository r;

  public AdmissionService(AdmissionRepository r) {
    this.r = r;
  }

  public List<Admission> all() {
    return r.findAll();
  }

  public Admission one(Long id) {
    return r.findById(id).orElseThrow();
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  public Admission create(Admission x) {
    return r.save(x);
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  public Admission update(Long id, Admission x) {
    x.setId(id);
    return r.save(x);
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  public void delete(Long id) {
    r.deleteById(id);
  }
}
