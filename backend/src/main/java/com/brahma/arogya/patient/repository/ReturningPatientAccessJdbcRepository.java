package com.brahma.arogya.patient.repository;

import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;

@org.springframework.stereotype.Repository()
public class ReturningPatientAccessJdbcRepository {

  private final JdbcTemplate jdbc;

  public ReturningPatientAccessJdbcRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Map<String, Object> findPatientsForLink(Long patientId) {
    return jdbc.queryForMap("SELECT id FROM patients WHERE id=?", patientId);
  }

  public Integer countStaffAccountsForLink(Long patientId) {
    return jdbc.queryForObject(
        "SELECT COUNT(*) FROM staff_accounts WHERE patient_id=?", Integer.class, patientId);
  }

  public int insertStaffAccountsForLink(String username, Object value2, Long patientId) {
    return jdbc.update(
        "INSERT INTO staff_accounts(username,password_hash,role,patient_id)"
            + " VALUES(?,?,'PATIENT',?)",
        username,
        value2,
        patientId);
  }
}
