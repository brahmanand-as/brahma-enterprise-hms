package com.brahma.arogya.document.repository;

import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;

@org.springframework.stereotype.Repository()
public class ClinicalDocumentJdbcRepository {

  private final JdbcTemplate jdbc;

  public ClinicalDocumentJdbcRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public List<Map<String, Object>> findIssuedDocumentsForDocuments(long value1) {
    return jdbc.queryForList(
        "SELECT id,patient_id,document_type,reference_id,issued_by,issued_at FROM"
            + " issued_documents WHERE patient_id=? ORDER BY id DESC",
        value1);
  }

  public List<Map<String, Object>> findIssuedDocumentsForDocuments2() {
    return jdbc.queryForList(
        "SELECT id,patient_id,document_type,reference_id,issued_by,issued_at FROM"
            + " issued_documents ORDER BY id DESC");
  }

  public List<Map<String, Object>> findIssuedDocumentsForIssue(String type, Long referenceId) {
    return jdbc.queryForList(
        "SELECT id,patient_id FROM issued_documents WHERE document_type=? AND reference_id=?",
        type,
        referenceId);
  }

  public Map<String, Object> findPrescriptionsForIssue(Long referenceId) {
    return jdbc.queryForMap(
        "SELECT p.*,e.doctor_name,e.signed_by FROM prescriptions p JOIN encounters e ON"
            + " e.id=p.encounter_id WHERE p.id=? AND p.status='FINAL'",
        referenceId);
  }

  public List<Map<String, Object>> findPrescriptionItemsForIssue(Long referenceId) {
    return jdbc.queryForList(
        "SELECT * FROM prescription_items WHERE prescription_id=?", referenceId);
  }

  public Map<String, Object> findLabOrderItemsForIssue(Long referenceId) {
    return jdbc.queryForMap(
        "SELECT o.patient_id,i.* FROM lab_order_items i JOIN lab_orders o ON"
            + " o.id=i.lab_order_id WHERE i.id=? AND i.status IN ('VERIFIED','COMPLETED')",
        referenceId);
  }

  public Map<String, Object> findRadiologyOrdersForIssue(Long referenceId) {
    return jdbc.queryForMap(
        "SELECT * FROM radiology_orders WHERE id=? AND status='VERIFIED'", referenceId);
  }

  public Map<String, Object> findPatientsForIssue(long patient) {
    return jdbc.queryForMap("SELECT * FROM patients WHERE id=?", patient);
  }

  public String readHospitalSettingsForIssue() {
    return jdbc.queryForObject(
        "SELECT setting_value FROM hospital_settings WHERE setting_key='hospital_name'",
        String.class);
  }

  public int insertIssuedDocumentsForIssue(
      long patient, String type, Long referenceId, String token, String snapshot, String name) {
    return jdbc.update(
        "INSERT INTO"
            + " issued_documents(patient_id,document_type,reference_id,verification_token,content_text,issued_by)"
            + " VALUES(?,?,?,?,?,?)",
        patient,
        type,
        referenceId,
        token,
        snapshot,
        name);
  }

  public Long readGeneratedIdForIssue() {
    return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
  }

  public Map<String, Object> findIssuedDocumentsForPdf(long id) {
    return jdbc.queryForMap("SELECT * FROM issued_documents WHERE id=?", id);
  }

  public Map<String, Object> findIssuedDocumentsForVerify(String token) {
    return jdbc.queryForMap(
        "SELECT document_type,issued_at,issued_by,'VALID' status FROM issued_documents WHERE"
            + " verification_token=?",
        token);
  }
}
