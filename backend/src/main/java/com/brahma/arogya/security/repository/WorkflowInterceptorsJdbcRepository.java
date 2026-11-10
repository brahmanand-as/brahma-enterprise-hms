package com.brahma.arogya.security.repository;

import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;

@org.springframework.stereotype.Repository()
public class WorkflowInterceptorsJdbcRepository {

  private final JdbcTemplate jdbc;

  public WorkflowInterceptorsJdbcRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Long readMedicationAdministrationsForPreHandle(long id) {
    return jdbc.queryForObject(
        "SELECT admission_id FROM medication_administrations WHERE id=?", Long.class, id);
  }

  public Long readGeneratedIdForPreHandle(String table, long id) {
    return jdbc.queryForObject("SELECT patient_id FROM " + table + " WHERE id=?", Long.class, id);
  }

  public int insertAuditLogsForAfterCompletion(
      String name, String method, String value3, String requestURI, String value5) {
    return jdbc.update(
        "INSERT INTO audit_logs(username,action,entity_type,entity_id,details)"
            + " VALUES(?,?,?,? ,?)",
        name,
        method,
        value3,
        requestURI,
        value5);
  }
}
