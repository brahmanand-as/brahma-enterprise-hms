package com.brahma.arogya.insurance.mapper;

import com.brahma.arogya.insurance.dto.InsuranceClaimRequest;
import com.brahma.arogya.insurance.dto.InsuranceClaimResponse;
import com.brahma.arogya.insurance.entity.InsuranceClaim;

public final class InsuranceClaimMapper {

  public static InsuranceClaim toEntity(InsuranceClaimRequest value) {
    if (value == null) return null;
    var result = new InsuranceClaim();
    result.setId(value.getId());
    result.setPatientId(value.getPatientId());
    result.setEncounterId(value.getEncounterId());
    result.setProvider(value.getProvider());
    result.setPolicyNo(value.getPolicyNo());
    result.setClaimAmount(value.getClaimAmount());
    result.setApprovedAmount(value.getApprovedAmount());
    result.setStatus(value.getStatus());
    return result;
  }

  public static InsuranceClaimResponse toResponse(InsuranceClaim value) {
    if (value == null) return null;
    var result = new InsuranceClaimResponse();
    result.setId(value.getId());
    result.setPatientId(value.getPatientId());
    result.setEncounterId(value.getEncounterId());
    result.setProvider(value.getProvider());
    result.setPolicyNo(value.getPolicyNo());
    result.setClaimAmount(value.getClaimAmount());
    result.setApprovedAmount(value.getApprovedAmount());
    result.setStatus(value.getStatus());
    return result;
  }
}
