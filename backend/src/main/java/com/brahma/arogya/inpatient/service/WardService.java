package com.brahma.arogya.inpatient.service;

import com.brahma.arogya.inpatient.dto.WardAdministrationRequest;
import com.brahma.arogya.inpatient.dto.WardMedicationRequest;
import com.brahma.arogya.inpatient.dto.WardTransferRequest;
import com.brahma.arogya.inpatient.repository.WardJdbcRepository;
import java.security.Principal;
import java.time.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@org.springframework.stereotype.Service()
public class WardService {

  private final BedChargeService bedCharges;

  private final WardJdbcRepository db;

  public WardService(WardJdbcRepository db, BedChargeService bedCharges) {
    this.db = db;
    this.bedCharges = bedCharges;
  }

  public List<Map<String, Object>> medications() {
    return db.findMedicationAdministrationsForMedications();
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  public Map<String, String> order(WardMedicationRequest r, Principal p) {
    if (r.admissionId() == null
        || r.medicineName() == null
        || r.medicineName().isBlank()
        || r.dose() == null
        || r.route() == null
        || r.scheduledAt() == null)
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Admission, medicine, dose, route and time required");
    if (db.countAdmissionsForOrder(r.admissionId()) == 0)
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Admission is not active");
    db.insertMedicationAdministrationsForOrder(
        r.admissionId(), r.medicineName(), r.dose(), r.route(), r.scheduledAt(), p.getName());
    return Map.of("status", "ORDERED");
  }

  @Transactional
  public Map<String, String> administer(long id, WardAdministrationRequest r, Principal p) {
    if (!Set.of("GIVEN", "HELD", "REFUSED", "MISSED").contains(r.status()))
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid administration status");
    var dose = db.findMedicationAdministrationsForAdminister(id);
    if (dose.get("medication_order_id") == null)
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Use a pharmacist-verified medication order");
    var order = db.findMedicationOrdersForAdminister(dose.get("medication_order_id"));
    if (!"VERIFIED".equals(order.get("status")))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Pharmacist verification required");
    db.findAdmissionsForAdminister(dose.get("admission_id"));
    if (!"GIVEN".equals(r.status()) && (r.notes() == null || r.notes().isBlank()))
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Clinical reason required for held/refused/missed medication");
    if ("GIVEN".equals(r.status())) {
      Object scheduled = dose.get("scheduled_at");
      java.time.LocalDateTime at =
          scheduled instanceof java.time.LocalDateTime d
              ? d
              : ((java.sql.Timestamp) scheduled).toLocalDateTime();
      long early = Long.parseLong(db.readHospitalSettingsForAdminister());
      if (at.isAfter(java.time.LocalDateTime.now().plusMinutes(early)))
        throw new ResponseStatusException(
            HttpStatus.CONFLICT, "Dose is outside the allowed early-administration window");
      java.math.BigDecimal stock = db.readPharmacyDispensesForAdminister(order.get("id"));
      long given = db.countMedicationAdministrationsForAdminister(order.get("id"));
      if (stock.compareTo(
              ((java.math.BigDecimal) order.get("quantity_per_dose"))
                  .multiply(java.math.BigDecimal.valueOf(given + 1)))
          < 0)
        throw new ResponseStatusException(
            HttpStatus.CONFLICT, "Dispense enough medication before administration");
    }
    int n = db.updateMedicationAdministrationsForAdminister(r.status(), r.notes(), p.getName(), id);
    if (n == 0)
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Medication already recorded or missing");
    return Map.of("status", r.status());
  }

  @Transactional
  public Map<String, String> transfer(long id, WardTransferRequest r, Principal p) {
    var a = db.findAdmissionsForTransfer(id);
    if (!"ADMITTED".equals(a.get("status")) || r.bedId() == null)
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Active admission and destination required");
    var b = db.findIpdBedsForTransfer(r.bedId());
    if (!"AVAILABLE".equals(b.get("status")) || Boolean.TRUE.equals(b.get("occupied")))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Destination unavailable");
    bedCharges.close(id);
    bedCharges.open(id, r.bedId());
    db.updateIpdBedsForTransfer(a.get("ward"), a.get("bed_no"));
    db.updateIpdBedsForTransfer2(r.bedId());
    db.updateAdmissionsForTransfer(b.get("ward"), b.get("bed_no"), id);
    db.insertBedMovementsForTransfer(
        id,
        a.get("ward") + "/" + a.get("bed_no"),
        b.get("ward") + "/" + b.get("bed_no"),
        p.getName());
    return Map.of("status", "TRANSFERRED");
  }

  public Map<String, String> ready(long id) {
    if (db.updateIpdBedsForReady(id) == 0)
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Bed is not awaiting cleaning");
    return Map.of("status", "AVAILABLE");
  }
}
