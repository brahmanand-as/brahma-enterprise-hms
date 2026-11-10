package com.brahma.arogya.inpatient.repository;

import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;

@org.springframework.stereotype.Repository()
public class BedChargeJdbcRepository {

  private final JdbcTemplate jdbc;

  public BedChargeJdbcRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public int insertAdmissionBedSegmentsForOpen(long admission, long bed) {
    return jdbc.update(
        "INSERT INTO admission_bed_segments(admission_id,bed_id,daily_rate) SELECT ?,id,daily_rate"
            + " FROM ipd_beds WHERE id=?",
        admission,
        bed);
  }

  public List<Map<String, Object>> findAdmissionBedSegmentsForClose(long admission) {
    return jdbc.queryForList(
        "SELECT * FROM admission_bed_segments WHERE admission_id=? AND ended_at IS NULL FOR"
            + " UPDATE",
        admission);
  }

  public Map<String, Object> findAdmissionsForClose(long admission) {
    return jdbc.queryForMap("SELECT patient_id,encounter_id FROM admissions WHERE id=?", admission);
  }

  public int updateAdmissionBedSegmentsForClose(long invoice, Object id) {
    return jdbc.update(
        "UPDATE admission_bed_segments SET ended_at=NOW(),invoice_id=? WHERE id=?", invoice, id);
  }
}
