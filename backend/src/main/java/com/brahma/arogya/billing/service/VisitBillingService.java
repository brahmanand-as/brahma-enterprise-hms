package com.brahma.arogya.billing.service;

import com.brahma.arogya.billing.repository.VisitBillingJdbcRepository;
import java.math.*;
import java.security.Principal;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@org.springframework.stereotype.Service()
public class VisitBillingService {

  private final VisitBillingJdbcRepository db;

  private final RevenueLedger ledger;

  public VisitBillingService(VisitBillingJdbcRepository db, RevenueLedger ledger) {
    this.db = db;
    this.ledger = ledger;
  }

  @Transactional
  public Map<String, String> billing(long id, Principal p) {
    var e = db.findEncountersForBilling(id);
    if (!"IN_CONSULTATION".equals(e.get("status")))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Visit is not in consultation");
    if (db.countConsultationsForBilling(id) == 0)
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Save clinical notes before signing");
    var fees = db.findDoctorProfilesForBilling(e.get("doctor_employee_id"));
    BigDecimal fee =
        fees.isEmpty() || fees.get(0) == null
            ? new BigDecimal(db.readHospitalSettingsForBilling())
            : fees.get(0);
    ledger.charge(
        ((Number) e.get("patient_id")).longValue(),
        id,
        "CONSULTATION-" + id,
        "CONSULTATION",
        id,
        "Doctor consultation",
        BigDecimal.ONE,
        fee);
    db.updateEncountersForBilling(p.getName(), id);
    ledger.completeVisit(id);
    return Map.of("status", db.readEncountersForBilling(id));
  }
}
