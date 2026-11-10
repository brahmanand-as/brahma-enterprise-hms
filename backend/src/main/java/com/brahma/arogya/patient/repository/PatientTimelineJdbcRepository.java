package com.brahma.arogya.patient.repository;

import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;

@org.springframework.stereotype.Repository()
public class PatientTimelineJdbcRepository {

  private final JdbcTemplate jdbc;

  public PatientTimelineJdbcRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Map<String, Object> findPatientsForTimeline(long id) {
    return jdbc.queryForMap("SELECT * FROM patients WHERE id=?", id);
  }

  public List<Map<String, Object>> findEncountersForTimeline(long id) {
    return jdbc.queryForList(
        "SELECT * FROM encounters WHERE patient_id=? ORDER BY started_at DESC", id);
  }

  public List<Map<String, Object>> findConsultationsForTimeline(long id) {
    return jdbc.queryForList(
        "SELECT c.*,e.encounter_no,e.doctor_name FROM consultations c JOIN encounters e ON"
            + " e.id=c.encounter_id WHERE e.patient_id=? ORDER BY c.id DESC",
        id);
  }

  public List<Map<String, Object>> findVitalsForTimeline(long id) {
    return jdbc.queryForList(
        "SELECT v.* FROM vitals v JOIN encounters e ON e.id=v.encounter_id WHERE e.patient_id=?"
            + " ORDER BY v.id DESC",
        id);
  }

  public List<Map<String, Object>> findIssuedDocumentsForTimeline(long id) {
    return jdbc.queryForList(
        "SELECT id,document_type,issued_by,issued_at FROM issued_documents WHERE patient_id=?"
            + " ORDER BY issued_at DESC",
        id);
  }
}
