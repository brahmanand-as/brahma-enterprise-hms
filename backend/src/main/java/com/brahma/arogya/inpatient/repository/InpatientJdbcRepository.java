package com.brahma.arogya.inpatient.repository;

import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;

@org.springframework.stereotype.Repository()
public class InpatientJdbcRepository {

  private final JdbcTemplate jdbc;

  public InpatientJdbcRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public List<Map<String, Object>> findIpdBedsForBeds() {
    return jdbc.queryForList(
        "SELECT b.*,p.uhid reserved_mrn,a.admission_no,pa.id current_patient_id,pa.uhid"
            + " current_mrn,CONCAT(pa.first_name,' ',COALESCE(pa.last_name,'')) patient_name"
            + " FROM ipd_beds b LEFT JOIN patients p ON p.id=b.reserved_patient_id LEFT JOIN"
            + " admissions a ON a.ward=b.ward AND a.bed_no=b.bed_no AND a.status='ADMITTED'"
            + " LEFT JOIN patients pa ON pa.id=a.patient_id ORDER BY"
            + " b.ward,b.floor_name,b.bed_no");
  }

  public int insertIpdBedsForBed(
      String ward, String bedNo, String floorName, String bedType, BigDecimal dailyRate) {
    return jdbc.update(
        "INSERT INTO ipd_beds(ward,bed_no,floor_name,bed_type,daily_rate) VALUES(?,?,?,?,?)",
        ward,
        bedNo,
        floorName,
        bedType,
        dailyRate);
  }

  public Map<String, Object> findIpdBedsForUpdateBed(long id) {
    return jdbc.queryForMap("SELECT * FROM ipd_beds WHERE id=? FOR UPDATE", id);
  }

  public int updateIpdBedsForUpdateBed(
      String ward, String bedNo, String floorName, String bedType, BigDecimal dailyRate, long id) {
    return jdbc.update(
        "UPDATE ipd_beds SET ward=?,bed_no=?,floor_name=?,bed_type=?,daily_rate=? WHERE id=?",
        ward,
        bedNo,
        floorName,
        bedType,
        dailyRate,
        id);
  }

  public Map<String, Object> findIpdBedsForBedState(long id) {
    return jdbc.queryForMap("SELECT * FROM ipd_beds WHERE id=? FOR UPDATE", id);
  }

  public Integer countPatientsForBedState(Long patientId) {
    return jdbc.queryForObject(
        "SELECT COUNT(*) FROM patients WHERE id=?", Integer.class, patientId);
  }

  public int updateIpdBedsForBedState(String status, Object value2, long id) {
    return jdbc.update(
        "UPDATE ipd_beds SET status=?,reserved_patient_id=? WHERE id=?", status, value2, id);
  }

  public List<Map<String, Object>> findAdmissionRecommendationsForRecommendations() {
    return jdbc.queryForList(
        "SELECT r.*,p.uhid,p.first_name FROM admission_recommendations r JOIN patients p ON"
            + " p.id=r.patient_id ORDER BY r.id DESC");
  }

  public Map<String, Object> findEncountersForRecommend(Long encounterId) {
    return jdbc.queryForMap("SELECT * FROM encounters WHERE id=?", encounterId);
  }

  public int insertAdmissionRecommendationsForRecommend(
      Long encounterId, Object patientId, String wardType, String reason, String name) {
    return jdbc.update(
        "INSERT INTO"
            + " admission_recommendations(encounter_id,patient_id,ward_type,reason,recommended_by)"
            + " VALUES(?,?,?,?,?)",
        encounterId,
        patientId,
        wardType,
        reason,
        name);
  }

  public Map<String, Object> findAdmissionRecommendationsForAllocate(long id) {
    return jdbc.queryForMap("SELECT * FROM admission_recommendations WHERE id=? FOR UPDATE", id);
  }

  public Map<String, Object> findAdmissionsForAllocate(Long admissionId) {
    return jdbc.queryForMap("SELECT * FROM admissions WHERE id=?", admissionId);
  }

  public int updateAdmissionRecommendationsForAllocate(Long admissionId, long id) {
    return jdbc.update(
        "UPDATE admission_recommendations SET status='ALLOCATED',admission_id=? WHERE id=?",
        admissionId,
        id);
  }

  public List<Map<String, Object>> findPharmacyDispensesForMedications() {
    return jdbc.queryForList(
        "SELECT o.*,p.uhid,p.first_name,a.ward,a.bed_no,COALESCE((SELECT SUM(d.quantity-d.returned)"
            + " FROM pharmacy_dispenses d WHERE d.medication_order_id=o.id),0) dispensed_quantity"
            + " FROM medication_orders o JOIN patients p ON p.id=o.patient_id JOIN admissions a ON"
            + " a.id=o.admission_id ORDER BY o.id DESC");
  }

  public Map<String, Object> findAdmissionsForOrder(Long admissionId) {
    return jdbc.queryForMap(
        "SELECT * FROM admissions WHERE id=? AND status='ADMITTED'", admissionId);
  }

  public String readInventoryItemsForOrder(Long inventoryItemId) {
    return jdbc.queryForObject(
        "SELECT name FROM inventory_items WHERE id=?", String.class, inventoryItemId);
  }

  public int insertMedicationOrdersForOrder(
      Long admissionId,
      Object patientId,
      Long inventoryItemId,
      String name,
      String dose,
      String route,
      LocalDateTime startsAt,
      Integer intervalHours,
      Integer doseCount,
      BigDecimal quantityPerDose,
      String name2) {
    return jdbc.update(
        "INSERT INTO"
            + " medication_orders(admission_id,patient_id,inventory_item_id,medicine_name,dose,route,starts_at,interval_hours,dose_count,quantity_per_dose,ordered_by)"
            + " VALUES(?,?,?,?,?,?,?,?,?,?,?)",
        admissionId,
        patientId,
        inventoryItemId,
        name,
        dose,
        route,
        startsAt,
        intervalHours,
        doseCount,
        quantityPerDose,
        name2);
  }

  public Long readGeneratedIdForOrder() {
    return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
  }

  public int insertMedicationAdministrationsForOrder2(
      Long admissionId,
      long id,
      String name,
      String dose,
      String route,
      Object value6,
      String name2) {
    return jdbc.update(
        "INSERT INTO"
            + " medication_administrations(admission_id,medication_order_id,medicine_name,dose,route,scheduled_at,ordered_by)"
            + " VALUES(?,?,?,?,?,?,?)",
        admissionId,
        id,
        name,
        dose,
        route,
        value6,
        name2);
  }

  public int updateMedicationOrdersForVerify(String name, long id) {
    return jdbc.update(
        "UPDATE medication_orders SET status='VERIFIED',verified_by=?,verified_at=NOW() WHERE"
            + " id=? AND status='ORDERED'",
        name,
        id);
  }

  public List<Map<String, Object>> findNursingNotesForNotes() {
    return jdbc.queryForList(
        "SELECT n.*,a.patient_id,a.ward,a.bed_no FROM nursing_notes n JOIN admissions a ON"
            + " a.id=n.admission_id ORDER BY n.id DESC");
  }

  public Map<String, Object> findAdmissionsForNote(Long admissionId) {
    return jdbc.queryForMap(
        "SELECT id FROM admissions WHERE id=? AND status='ADMITTED'", admissionId);
  }

  public int insertNursingNotesForNote(
      Long admissionId, String noteType, String assessment, String plan, String name) {
    return jdbc.update(
        "INSERT INTO nursing_notes(admission_id,note_type,assessment,plan,nurse) VALUES(?,?,?,?,?)",
        admissionId,
        noteType,
        assessment,
        plan,
        name);
  }
}
