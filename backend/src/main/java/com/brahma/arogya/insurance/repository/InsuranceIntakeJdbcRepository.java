package com.brahma.arogya.insurance.repository;

import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;

@org.springframework.stereotype.Repository()
public class InsuranceIntakeJdbcRepository {

  private final JdbcTemplate jdbc;

  public InsuranceIntakeJdbcRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Map<String, Object> findAdmissionsForCreate(Long admissionId) {
    return jdbc.queryForMap("SELECT * FROM admissions WHERE id=?", admissionId);
  }

  public int insertInsuranceClaimsForCreate(
      Object patientId,
      Object encounterId,
      Long admissionId,
      String provider,
      String policyNo,
      BigDecimal claimAmount,
      BigDecimal nonPayable,
      String claimType) {
    return jdbc.update(
        "INSERT INTO"
            + " insurance_claims(patient_id,encounter_id,admission_id,provider,policy_no,claim_amount,non_payable,approved_amount,claim_type,status)"
            + " VALUES(?,?,?,?,?,?,?,0,?,'DRAFT')",
        patientId,
        encounterId,
        admissionId,
        provider,
        policyNo,
        claimAmount,
        nonPayable,
        claimType);
  }

  public Long readGeneratedIdForCreate() {
    return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
  }

  public Map<String, Object> findInsuranceClaimsForCreate(long id) {
    return jdbc.queryForMap("SELECT * FROM insurance_claims WHERE id=?", id);
  }
}
