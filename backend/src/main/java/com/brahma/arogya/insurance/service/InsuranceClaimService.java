package com.brahma.arogya.insurance.service;

import com.brahma.arogya.insurance.entity.InsuranceClaim;
import com.brahma.arogya.insurance.repository.InsuranceClaimRepository;
import java.util.*;

@org.springframework.stereotype.Service()
public class InsuranceClaimService {

  private final InsuranceClaimRepository r;

  public InsuranceClaimService(InsuranceClaimRepository r) {
    this.r = r;
  }

  public List<InsuranceClaim> all() {
    return r.findAll();
  }

  public InsuranceClaim one(Long id) {
    return r.findById(id).orElseThrow();
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  public InsuranceClaim create(InsuranceClaim x) {
    return r.save(x);
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  public InsuranceClaim update(Long id, InsuranceClaim x) {
    x.setId(id);
    return r.save(x);
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  public void delete(Long id) {
    r.deleteById(id);
  }
}
