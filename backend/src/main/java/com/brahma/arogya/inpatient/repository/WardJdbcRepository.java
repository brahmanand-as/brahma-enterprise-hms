package com.brahma.arogya.inpatient.repository;

import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;

@org.springframework.stereotype.Repository()
public class WardJdbcRepository {

  private final JdbcTemplate jdbc;

  public WardJdbcRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public List<Map<String, Object>> findMedicationAdministrationsForMedications() {
    return jdbc.queryForList("SELECT * FROM medication_administrations ORDER BY scheduled_at");
  }

  public Integer countAdmissionsForOrder(Long admissionId) {
    return jdbc.queryForObject(
        "SELECT COUNT(*) FROM admissions WHERE id=? AND status='ADMITTED'",
        Integer.class,
        admissionId);
  }

  public int insertMedicationAdministrationsForOrder(
      Long admissionId,
      String medicineName,
      String dose,
      String route,
      LocalDateTime scheduledAt,
      String name) {
    return jdbc.update(
        "INSERT INTO"
            + " medication_administrations(admission_id,medicine_name,dose,route,scheduled_at,ordered_by)"
            + " VALUES(?,?,?,?,?,?)",
        admissionId,
        medicineName,
        dose,
        route,
        scheduledAt,
        name);
  }

  public Map<String, Object> findMedicationAdministrationsForAdminister(long id) {
    return jdbc.queryForMap("SELECT * FROM medication_administrations WHERE id=? FOR UPDATE", id);
  }

  public Map<String, Object> findMedicationOrdersForAdminister(Object medicationOrderId) {
    return jdbc.queryForMap(
        "SELECT * FROM medication_orders WHERE id=? FOR UPDATE", medicationOrderId);
  }

  public Map<String, Object> findAdmissionsForAdminister(Object admissionId) {
    return jdbc.queryForMap(
        "SELECT id FROM admissions WHERE id=? AND status='ADMITTED'", admissionId);
  }

  public String readHospitalSettingsForAdminister() {
    return jdbc.queryForObject(
        "SELECT setting_value FROM hospital_settings WHERE"
            + " setting_key='early_medication_minutes'",
        String.class);
  }

  public BigDecimal readPharmacyDispensesForAdminister(Object id) {
    return jdbc.queryForObject(
        "SELECT COALESCE(SUM(quantity-returned),0) FROM pharmacy_dispenses WHERE"
            + " medication_order_id=?",
        BigDecimal.class,
        id);
  }

  public Long countMedicationAdministrationsForAdminister(Object id) {
    return jdbc.queryForObject(
        "SELECT COUNT(*) FROM medication_administrations WHERE medication_order_id=? AND"
            + " status='GIVEN'",
        Long.class,
        id);
  }

  public int updateMedicationAdministrationsForAdminister(
      String status, String notes, String name, long id) {
    return jdbc.update(
        "UPDATE medication_administrations SET"
            + " status=?,notes=?,administered_by=?,administered_at=NOW() WHERE id=? AND"
            + " status='ORDERED'",
        status,
        notes,
        name,
        id);
  }

  public Map<String, Object> findAdmissionsForTransfer(long id) {
    return jdbc.queryForMap("SELECT * FROM admissions WHERE id=? FOR UPDATE", id);
  }

  public Map<String, Object> findIpdBedsForTransfer(Long bedId) {
    return jdbc.queryForMap("SELECT * FROM ipd_beds WHERE id=? FOR UPDATE", bedId);
  }

  public int updateIpdBedsForTransfer(Object ward, Object bedNo) {
    return jdbc.update(
        "UPDATE ipd_beds SET occupied=0,status='CLEANING' WHERE ward=? AND bed_no=?", ward, bedNo);
  }

  public int updateIpdBedsForTransfer2(Long bedId) {
    return jdbc.update("UPDATE ipd_beds SET occupied=1,status='OCCUPIED' WHERE id=?", bedId);
  }

  public int updateAdmissionsForTransfer(Object ward, Object bedNo, long id) {
    return jdbc.update("UPDATE admissions SET ward=?,bed_no=? WHERE id=?", ward, bedNo, id);
  }

  public int insertBedMovementsForTransfer(long id, String value2, Object value3, String name) {
    return jdbc.update(
        "INSERT INTO bed_movements(admission_id,from_bed,to_bed,action,performed_by)"
            + " VALUES(?,?,?,'TRANSFER',?)",
        id,
        value2,
        value3,
        name);
  }

  public int updateIpdBedsForReady(long id) {
    return jdbc.update(
        "UPDATE ipd_beds SET status='AVAILABLE' WHERE id=? AND occupied=0 AND"
            + " status='CLEANING'",
        id);
  }
}
