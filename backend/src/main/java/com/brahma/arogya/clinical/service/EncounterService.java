package com.brahma.arogya.clinical.service;

import com.brahma.arogya.clinical.entity.Encounter;
import com.brahma.arogya.clinical.repository.EncounterRepository;
import java.util.*;

@org.springframework.stereotype.Service()
public class EncounterService {

  private final EncounterRepository r;

  public EncounterService(EncounterRepository r) {
    this.r = r;
  }

  public List<Encounter> all() {
    return r.findAll();
  }

  public Encounter one(Long id) {
    return r.findById(id).orElseThrow();
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  public Encounter create(Encounter x) {
    return r.save(x);
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  public Encounter update(Long id, Encounter x) {
    x.setId(id);
    return r.save(x);
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  public void delete(Long id) {
    r.deleteById(id);
  }
}
