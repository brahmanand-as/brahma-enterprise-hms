package com.brahma.arogya.analytics.service;

import com.brahma.arogya.billing.repository.InvoiceRepository;
import com.brahma.arogya.clinical.repository.EncounterRepository;
import com.brahma.arogya.laboratory.repository.LabOrderRepository;
import com.brahma.arogya.patient.repository.PatientRepository;
import java.util.*;

@org.springframework.stereotype.Service()
public class DashboardService {

  private final PatientRepository p;

  private final EncounterRepository e;

  private final LabOrderRepository l;

  private final InvoiceRepository i;

  DashboardService(
      PatientRepository p, EncounterRepository e, LabOrderRepository l, InvoiceRepository i) {
    this.p = p;
    this.e = e;
    this.l = l;
    this.i = i;
  }

  public Map<String, Long> stats() {
    return Map.of(
        "patients",
        p.count(),
        "encounters",
        e.count(),
        "labOrders",
        l.count(),
        "invoices",
        i.count());
  }
}
