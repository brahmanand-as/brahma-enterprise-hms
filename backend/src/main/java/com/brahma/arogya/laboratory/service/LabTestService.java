package com.brahma.arogya.laboratory.service;

import com.brahma.arogya.laboratory.entity.LabTest;
import com.brahma.arogya.laboratory.repository.LabTestRepository;
import java.util.*;

@org.springframework.stereotype.Service()
public class LabTestService {

  private final LabTestRepository r;

  public LabTestService(LabTestRepository r) {
    this.r = r;
  }

  public List<LabTest> all() {
    return r.findAll();
  }

  public LabTest one(Long id) {
    return r.findById(id).orElseThrow();
  }

  public LabTest create(LabTest x) {
    return r.save(x);
  }

  public LabTest update(Long id, LabTest x) {
    x.setId(id);
    return r.save(x);
  }

  public void delete(Long id) {
    r.deleteById(id);
  }
}
