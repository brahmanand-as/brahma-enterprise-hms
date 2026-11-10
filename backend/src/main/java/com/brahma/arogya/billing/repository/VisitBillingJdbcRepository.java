package com.brahma.arogya.billing.repository;

import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;

@org.springframework.stereotype.Repository()
public class VisitBillingJdbcRepository {

  private final JdbcTemplate jdbc;

  public VisitBillingJdbcRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Map<String, Object> findEncountersForBilling(long id) {
    return jdbc.queryForMap("SELECT * FROM encounters WHERE id=? FOR UPDATE", id);
  }

  public Integer countConsultationsForBilling(long id) {
    return jdbc.queryForObject(
        "SELECT COUNT(*) FROM consultations WHERE encounter_id=?", Integer.class, id);
  }

  public List<BigDecimal> findDoctorProfilesForBilling(Object doctorEmployeeId) {
    return jdbc.queryForList(
        "SELECT consultation_fee FROM doctor_profiles WHERE employee_id=?",
        BigDecimal.class,
        doctorEmployeeId);
  }

  public String readHospitalSettingsForBilling() {
    return jdbc.queryForObject(
        "SELECT setting_value FROM hospital_settings WHERE"
            + " setting_key='default_consultation_fee'",
        String.class);
  }

  public int updateEncountersForBilling(String name, long id) {
    return jdbc.update(
        "UPDATE encounters SET status='BILLING',signed_by=?,signed_at=NOW() WHERE id=?", name, id);
  }

  public String readEncountersForBilling(long id) {
    return jdbc.queryForObject("SELECT status FROM encounters WHERE id=?", String.class, id);
  }
}
