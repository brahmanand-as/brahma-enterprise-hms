package com.brahma.arogya.clinical.repository;

import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;

@org.springframework.stereotype.Repository()
public class ClinicalWorkflowJdbcRepository {

  private final JdbcTemplate jdbc;

  public ClinicalWorkflowJdbcRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public int updateEncountersForStartConsultation(Long id) {
    return jdbc.update("UPDATE encounters SET consultation_at=NOW() WHERE id=?", id);
  }

  public Integer countConsultationsForComplete(Long id) {
    return jdbc.queryForObject(
        "SELECT COUNT(*) FROM consultations WHERE encounter_id=?", Integer.class, id);
  }

  public Integer countInvoicesForComplete(Long id) {
    return jdbc.queryForObject(
        "SELECT COUNT(*) FROM invoices WHERE encounter_id=? AND status<>'PAID'", Integer.class, id);
  }

  public int updateEncountersForComplete(String name, Long id) {
    return jdbc.update("UPDATE encounters SET signed_by=?,signed_at=NOW() WHERE id=?", name, id);
  }
}
