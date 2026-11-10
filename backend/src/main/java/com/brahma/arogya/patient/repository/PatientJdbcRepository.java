package com.brahma.arogya.patient.repository;

import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;

@org.springframework.stereotype.Repository()
public class PatientJdbcRepository {

  private final JdbcTemplate jdbc;

  public PatientJdbcRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Long readPatientNumberSequenceForCreate() {
    return jdbc.queryForObject(
        "SELECT next_value FROM patient_number_sequence WHERE id=1 FOR UPDATE", Long.class);
  }

  public int updatePatientNumberSequenceForCreate() {
    return jdbc.update("UPDATE patient_number_sequence SET next_value=next_value+1 WHERE id=1");
  }

  public String readHospitalSettingsForCreate() {
    return jdbc.queryForObject(
        "SELECT setting_value FROM hospital_settings WHERE" + " setting_key='registration_fee'",
        String.class);
  }
}
