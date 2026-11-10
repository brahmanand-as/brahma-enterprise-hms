package com.brahma.arogya.billing.service;

import com.brahma.arogya.billing.entity.Invoice;
import com.brahma.arogya.billing.repository.InvoiceRepository;
import java.util.*;

@org.springframework.stereotype.Service()
public class InvoiceService {

  private final InvoiceRepository r;

  public InvoiceService(InvoiceRepository r) {
    this.r = r;
  }

  public List<Invoice> all() {
    return r.findAll();
  }

  public Invoice one(Long id) {
    return r.findById(id).orElseThrow();
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  public Invoice create(Invoice x) {
    return r.save(x);
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  public Invoice update(Long id, Invoice x) {
    x.setId(id);
    return r.save(x);
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  public void delete(Long id) {
    r.deleteById(id);
  }
}
