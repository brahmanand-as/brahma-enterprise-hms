package com.brahma.arogya.insurance.service;

import com.brahma.arogya.insurance.dto.InsuranceIntakeRequest;
import com.brahma.arogya.insurance.repository.InsuranceIntakeJdbcRepository;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@org.springframework.stereotype.Service()
public class InsuranceIntakeService {

  private final InsuranceIntakeJdbcRepository db;

  public InsuranceIntakeService(InsuranceIntakeJdbcRepository db) {
    this.db = db;
  }

  @Transactional
  public Map<String, Object> create(InsuranceIntakeRequest r) {
    if (r.admissionId() == null
        || r.provider() == null
        || r.provider().isBlank()
        || r.policyNo() == null
        || !Set.of("CASHLESS", "REIMBURSEMENT").contains(r.claimType())
        || r.claimAmount() == null
        || r.claimAmount().signum() < 0
        || r.nonPayable() == null
        || r.nonPayable().signum() < 0
        || r.nonPayable().compareTo(r.claimAmount()) > 0)
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Valid admission, provider, policy, claim type and amounts required");
    var a = db.findAdmissionsForCreate(r.admissionId());
    db.insertInsuranceClaimsForCreate(
        a.get("patient_id"),
        a.get("encounter_id"),
        r.admissionId(),
        r.provider(),
        r.policyNo(),
        r.claimAmount(),
        r.nonPayable(),
        r.claimType());
    long id = db.readGeneratedIdForCreate();
    return db.findInsuranceClaimsForCreate(id);
  }
}
