package com.brahma.arogya.clinical.service;

import com.brahma.arogya.clinical.entity.Consultation;
import com.brahma.arogya.clinical.repository.ConsultationRepository;
import java.util.*;

@org.springframework.stereotype.Service()
public class ConsultationService {

  private final ConsultationRepository r;

  public ConsultationService(ConsultationRepository r) {
    this.r = r;
  }

  public List<Consultation> all() {
    return r.findAll();
  }

  public Consultation one(Long id) {
    return r.findById(id).orElseThrow();
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  public Consultation create(Consultation x) {
    return r.save(x);
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  public Consultation update(Long id, Consultation x) {
    x.setId(id);
    return r.save(x);
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  public void delete(Long id) {
    r.deleteById(id);
  }
}
