package com.brahma.arogya.clinical.repository;

import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;

@org.springframework.stereotype.Repository()
public class ClinicalOrdersJdbcRepository {

  private final JdbcTemplate jdbc;

  public ClinicalOrdersJdbcRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Map<String, Object> findEncountersForRadiology(Long encounterId) {
    return jdbc.queryForMap("SELECT * FROM encounters WHERE id=?", encounterId);
  }

  public int insertRadiologyOrdersForRadiology(
      String no, Long encounterId, Object patientId, String studyName) {
    return jdbc.update(
        "INSERT INTO radiology_orders(order_no,encounter_id,patient_id,study_name,status)"
            + " VALUES(?,?,?,?,'ORDERED')",
        no,
        encounterId,
        patientId,
        studyName);
  }

  public List<Map<String, Object>> findPatientsForAll() {
    return jdbc.queryForList(
        "SELECT pr.*,p.uhid,CONCAT(p.first_name,' ',COALESCE(p.last_name,'')) patient_name FROM"
            + " prescriptions pr JOIN patients p ON p.id=pr.patient_id ORDER BY pr.id DESC");
  }

  public Map<String, Object> findEncountersForCreate(Long encounterId) {
    return jdbc.queryForMap("SELECT * FROM encounters WHERE id=? FOR UPDATE", encounterId);
  }

  public int insertPrescriptionsForCreate(Long encounterId, Object patientId) {
    return jdbc.update(
        "INSERT INTO prescriptions(encounter_id,patient_id,status) VALUES(?,?,'DRAFT')",
        encounterId,
        patientId);
  }

  public Long readGeneratedIdForCreate() {
    return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
  }

  public int insertPrescriptionItemsForCreate(
      long id,
      Object medicineName,
      Object dose,
      Object frequency,
      Object duration,
      Object instructions) {
    return jdbc.update(
        "INSERT INTO"
            + " prescription_items(prescription_id,medicine_name,dose,frequency,duration,instructions)"
            + " VALUES(?,?,?,?,?,?)",
        id,
        medicineName,
        dose,
        frequency,
        duration,
        instructions);
  }

  public int updatePrescriptionsForFinalizeOrder(long id) {
    return jdbc.update("UPDATE prescriptions SET status='FINAL' WHERE id=? AND status='DRAFT'", id);
  }

  public int insertAuditLogsForFinalizeOrder(String name, String value2) {
    return jdbc.update(
        "INSERT INTO audit_logs(username,action,entity_type,entity_id)"
            + " VALUES(?,'PRESCRIPTION_FINALIZED','PRESCRIPTION',?)",
        name,
        value2);
  }

  public List<Map<String, Object>> findPrescriptionItemsForItems(long id) {
    return jdbc.queryForList("SELECT * FROM prescription_items WHERE prescription_id=?", id);
  }
}
