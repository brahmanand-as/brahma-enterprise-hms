package com.brahma.arogya.inpatient.service;

import com.brahma.arogya.billing.service.RevenueLedger;
import com.brahma.arogya.inpatient.dto.InpatientBedRequest;
import com.brahma.arogya.inpatient.dto.InpatientBedStatusRequest;
import com.brahma.arogya.inpatient.dto.InpatientLinkRequest;
import com.brahma.arogya.inpatient.dto.InpatientMedicationRequest;
import com.brahma.arogya.inpatient.dto.InpatientNoteRequest;
import com.brahma.arogya.inpatient.dto.InpatientRecommendationRequest;
import com.brahma.arogya.inpatient.repository.InpatientJdbcRepository;
import com.brahma.arogya.notification.service.CoordinationService;
import com.brahma.arogya.security.service.CareScope;
import java.math.*;
import java.security.Principal;
import java.time.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@org.springframework.stereotype.Service()
public class InpatientService {

  private final CareScope care;

  private final InpatientJdbcRepository db;

  private final CoordinationService notices;

  public InpatientService(InpatientJdbcRepository db, CoordinationService notices, CareScope care) {
    this.db = db;
    this.notices = notices;
    this.care = care;
  }

  public List<Map<String, Object>> beds() {
    var rows = db.findIpdBedsForBeds();
    for (var b : rows) {
      if (b.get("current_patient_id") instanceof Number n && !care.can(n.longValue())) {
        b.remove("current_mrn");
        b.remove("current_patient_id");
        b.remove("patient_name");
        b.remove("admission_no");
      }
      if (b.get("reserved_patient_id") instanceof Number n && !care.can(n.longValue()))
        b.remove("reserved_mrn");
    }
    return rows;
  }

  public Map<String, String> bed(InpatientBedRequest b) {
    if (b.ward() == null
        || b.ward().isBlank()
        || b.bedNo() == null
        || b.bedNo().isBlank()
        || b.floorName() == null
        || b.bedType() == null) throw bad("Ward, unique bed number, floor and bed type required");
    RevenueLedger.money(b.dailyRate(), true);
    db.insertIpdBedsForBed(b.ward(), b.bedNo(), b.floorName(), b.bedType(), b.dailyRate());
    return Map.of("status", "CREATED");
  }

  @Transactional
  public Map<String, String> updateBed(long id, InpatientBedRequest f) {
    var b = db.findIpdBedsForUpdateBed(id);
    if (Boolean.TRUE.equals(b.get("occupied")) || "RESERVED".equals(b.get("status")))
      throw conflict("Edit bed location and rates only while unoccupied and unreserved");
    if (f.ward() == null || f.bedNo() == null || f.floorName() == null || f.bedType() == null)
      throw bad("Bed location/type required");
    RevenueLedger.money(f.dailyRate(), true);
    db.updateIpdBedsForUpdateBed(
        f.ward(), f.bedNo(), f.floorName(), f.bedType(), f.dailyRate(), id);
    return Map.of("status", "UPDATED");
  }

  @Transactional
  public Map<String, String> bedState(long id, InpatientBedStatusRequest f, Principal p) {
    if (f.status() == null
        || !Set.of("AVAILABLE", "RESERVED", "CLEANING", "MAINTENANCE", "BLOCKED")
            .contains(f.status())
        || f.reason() == null
        || f.reason().isBlank()) throw bad("Bed status and reason required");
    var b = db.findIpdBedsForBedState(id);
    if (Boolean.TRUE.equals(b.get("occupied")))
      throw conflict("Transfer/discharge the occupied bed first");
    if ("RESERVED".equals(f.status())) {
      if (f.patientId() == null || db.countPatientsForBedState(f.patientId()) == 0)
        throw bad("Reservation requires a patient");
    }
    db.updateIpdBedsForBedState(
        f.status(), "RESERVED".equals(f.status()) ? f.patientId() : null, id);
    return Map.of("status", f.status());
  }

  public List<Map<String, Object>> recommendations() {
    return db.findAdmissionRecommendationsForRecommendations();
  }

  public Map<String, String> recommend(InpatientRecommendationRequest f, Principal p) {
    if (f.encounterId() == null
        || f.wardType() == null
        || f.reason() == null
        || f.reason().isBlank()) throw bad("Visit, recommended ward and clinical reason required");
    var e = db.findEncountersForRecommend(f.encounterId());
    db.insertAdmissionRecommendationsForRecommend(
        f.encounterId(), e.get("patient_id"), f.wardType(), f.reason(), p.getName());
    notices.department(
        "ipd.manage",
        "Admission recommendation",
        "A doctor has recommended admission for MRN patient " + e.get("patient_id"));
    return Map.of("status", "PENDING");
  }

  @Transactional
  public Map<String, String> allocate(long id, InpatientLinkRequest f) {
    var r = db.findAdmissionRecommendationsForAllocate(id);
    var a = db.findAdmissionsForAllocate(f.admissionId());
    if (!r.get("patient_id").equals(a.get("patient_id"))
        || !"PENDING".equals(r.get("status"))
        || !"ADMITTED".equals(a.get("status")))
      throw conflict("Active admission must match the pending recommendation");
    db.updateAdmissionRecommendationsForAllocate(f.admissionId(), id);
    return Map.of("status", "ALLOCATED");
  }

  public List<Map<String, Object>> medications() {
    return db.findPharmacyDispensesForMedications();
  }

  @Transactional
  public Map<String, Object> order(InpatientMedicationRequest f, Principal p) {
    if (f.admissionId() == null
        || f.inventoryItemId() == null
        || f.dose() == null
        || f.dose().isBlank()
        || f.route() == null
        || f.startsAt() == null
        || f.intervalHours() == null
        || f.intervalHours() < 1
        || f.intervalHours() > 168
        || f.doseCount() == null
        || f.doseCount() < 1
        || f.doseCount() > 180)
      throw bad("Admission, medicine, dose, route and valid dose schedule required");
    RevenueLedger.money(f.quantityPerDose(), false);
    var a = db.findAdmissionsForOrder(f.admissionId());
    String name = db.readInventoryItemsForOrder(f.inventoryItemId());
    db.insertMedicationOrdersForOrder(
        f.admissionId(),
        a.get("patient_id"),
        f.inventoryItemId(),
        name,
        f.dose(),
        f.route(),
        f.startsAt(),
        f.intervalHours(),
        f.doseCount(),
        f.quantityPerDose(),
        p.getName());
    long id = db.readGeneratedIdForOrder();
    for (int n = 0; n < f.doseCount(); n++)
      db.insertMedicationAdministrationsForOrder2(
          f.admissionId(),
          id,
          name,
          f.dose(),
          f.route(),
          f.startsAt().plusHours((long) n * f.intervalHours()),
          p.getName());
    notices.department(
        "pharmacy.manage",
        "Medication verification",
        "Medication order " + id + " requires pharmacist verification");
    return Map.of("id", id, "status", "ORDERED");
  }

  public Map<String, String> verify(long id, Principal p) {
    if (db.updateMedicationOrdersForVerify(p.getName(), id) == 0)
      throw conflict("Order not awaiting verification");
    return Map.of("status", "VERIFIED");
  }

  public List<Map<String, Object>> notes() {
    return db.findNursingNotesForNotes();
  }

  public Map<String, String> note(InpatientNoteRequest f, Principal p) {
    if (f.admissionId() == null
        || f.noteType() == null
        || !Set.of("ASSESSMENT", "HANDOVER", "CARE_PLAN").contains(f.noteType())
        || f.assessment() == null
        || f.assessment().isBlank()) throw bad("Admission, note type and assessment required");
    db.findAdmissionsForNote(f.admissionId());
    db.insertNursingNotesForNote(
        f.admissionId(), f.noteType(), f.assessment(), f.plan(), p.getName());
    return Map.of("status", "RECORDED");
  }

  private ResponseStatusException bad(String s) {
    return new ResponseStatusException(HttpStatus.BAD_REQUEST, s);
  }

  private ResponseStatusException conflict(String s) {
    return new ResponseStatusException(HttpStatus.CONFLICT, s);
  }
}
