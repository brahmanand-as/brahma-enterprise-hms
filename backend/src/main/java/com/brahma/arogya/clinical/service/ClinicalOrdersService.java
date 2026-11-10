package com.brahma.arogya.clinical.service;

import com.brahma.arogya.clinical.dto.ClinicalOrdersPrescriptionRequest;
import com.brahma.arogya.clinical.dto.ClinicalOrdersRadiologyRequest;
import com.brahma.arogya.clinical.repository.ClinicalOrdersJdbcRepository;
import com.brahma.arogya.document.dto.ClinicalDocumentIssueRequest;
import com.brahma.arogya.document.service.ClinicalDocumentService;
import java.security.Principal;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@org.springframework.stereotype.Service()
public class ClinicalOrdersService {

  private final ClinicalDocumentService documents;

  private final ClinicalOrdersJdbcRepository db;

  public ClinicalOrdersService(ClinicalOrdersJdbcRepository db, ClinicalDocumentService documents) {
    this.db = db;
    this.documents = documents;
  }

  @Transactional
  public Map<String, String> radiology(ClinicalOrdersRadiologyRequest r) {
    if (r.encounterId() == null || r.studyName() == null || r.studyName().isBlank())
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Encounter and study required");
    var e = db.findEncountersForRadiology(r.encounterId());
    if (!"IN_CONSULTATION".equals(e.get("status")))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Encounter not in consultation");
    String no = "RAD-" + UUID.randomUUID();
    db.insertRadiologyOrdersForRadiology(no, r.encounterId(), e.get("patient_id"), r.studyName());
    return Map.of("orderNo", no, "status", "ORDERED");
  }

  public List<Map<String, Object>> all() {
    return db.findPatientsForAll();
  }

  @Transactional
  public Map<String, Object> create(ClinicalOrdersPrescriptionRequest r, Principal p) {
    if (r.encounterId() == null || r.items() == null || r.items().isEmpty())
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Encounter and medicines required");
    var e = db.findEncountersForCreate(r.encounterId());
    if (!"IN_CONSULTATION".equals(e.get("status")))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Encounter not in consultation");
    for (var m : r.items())
      if (m.medicineName() == null
          || m.medicineName().isBlank()
          || m.dose() == null
          || m.frequency() == null
          || m.duration() == null)
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Medicine, dose, frequency and duration required");
    db.insertPrescriptionsForCreate(r.encounterId(), e.get("patient_id"));
    long id = db.readGeneratedIdForCreate();
    for (var m : r.items())
      db.insertPrescriptionItemsForCreate(
          id, m.medicineName(), m.dose(), m.frequency(), m.duration(), m.instructions());
    return Map.of("id", id, "status", "DRAFT");
  }

  @Transactional
  public Map<String, String> finalizeOrder(long id, Principal p) {
    if (db.updatePrescriptionsForFinalizeOrder(id) == 0)
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Prescription not a draft");
    db.insertAuditLogsForFinalizeOrder(p.getName(), String.valueOf(id));
    documents.issue(new ClinicalDocumentIssueRequest("PRESCRIPTION", id), p);
    return Map.of("status", "FINAL");
  }

  public List<Map<String, Object>> items(long id) {
    return db.findPrescriptionItemsForItems(id);
  }
}
