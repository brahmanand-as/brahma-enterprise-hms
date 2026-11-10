package com.brahma.arogya.patient.service;

import com.brahma.arogya.billing.service.RevenueLedger;
import com.brahma.arogya.patient.entity.Patient;
import com.brahma.arogya.patient.repository.PatientJdbcRepository;
import com.brahma.arogya.patient.repository.PatientRepository;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@org.springframework.stereotype.Service()
public class PatientService {

  private final RevenueLedger ledger;

  private final jakarta.servlet.http.HttpServletRequest request;

  private final PatientRepository r;

  private final PatientJdbcRepository db;

  public PatientService(
      PatientRepository r,
      PatientJdbcRepository db,
      RevenueLedger ledger,
      jakarta.servlet.http.HttpServletRequest request) {
    this.r = r;
    this.db = db;
    this.ledger = ledger;
    this.request = request;
  }

  public List<Patient> all() {
    return r.findAll();
  }

  public Patient one(Long id) {
    return r.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));
  }

  @Transactional
  public Patient create(Patient x) {
    if (x.getId() != null || x.getFirstName() == null || x.getFirstName().isBlank())
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "New patient name required; ID is generated");
    Long next = db.readPatientNumberSequenceForCreate();
    db.updatePatientNumberSequenceForCreate();
    x.setUhid(String.format("MRN-%08d", next));
    Patient saved = r.saveAndFlush(x);
    if (ledger != null
        && request != null
        && !request.getRequestURI().equals("/api/public/register")) {
      java.math.BigDecimal fee = new java.math.BigDecimal(db.readHospitalSettingsForCreate());
      ledger.charge(
          saved.getId(),
          null,
          "REGISTRATION-" + saved.getId(),
          "REGISTRATION",
          saved.getId(),
          "One-time MRN registration",
          java.math.BigDecimal.ONE,
          fee);
    }
    return saved;
  }

  public Patient update(Long id, Patient x) {
    Patient old = one(id);
    x.setId(id);
    x.setUhid(old.getUhid());
    if (x.getFirstName() == null || x.getFirstName().isBlank())
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name required");
    return r.save(x);
  }

  public void delete(Long id) {
    throw new ResponseStatusException(
        HttpStatus.CONFLICT, "Patient records must be retained for clinical audit");
  }
}
