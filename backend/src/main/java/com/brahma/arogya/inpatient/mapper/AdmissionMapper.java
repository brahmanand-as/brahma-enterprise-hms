package com.brahma.arogya.inpatient.mapper;

import com.brahma.arogya.inpatient.dto.AdmissionRequest;
import com.brahma.arogya.inpatient.dto.AdmissionResponse;
import com.brahma.arogya.inpatient.entity.Admission;

public final class AdmissionMapper {

  public static Admission toEntity(AdmissionRequest value) {
    if (value == null) return null;
    var result = new Admission();
    result.setId(value.getId());
    result.setAdmissionNo(value.getAdmissionNo());
    result.setEncounterId(value.getEncounterId());
    result.setPatientId(value.getPatientId());
    result.setWard(value.getWard());
    result.setBedNo(value.getBedNo());
    result.setStatus(value.getStatus());
    result.setAdmittedAt(value.getAdmittedAt());
    result.setDischargedAt(value.getDischargedAt());
    return result;
  }

  public static AdmissionResponse toResponse(Admission value) {
    if (value == null) return null;
    var result = new AdmissionResponse();
    result.setId(value.getId());
    result.setAdmissionNo(value.getAdmissionNo());
    result.setEncounterId(value.getEncounterId());
    result.setPatientId(value.getPatientId());
    result.setWard(value.getWard());
    result.setBedNo(value.getBedNo());
    result.setStatus(value.getStatus());
    result.setAdmittedAt(value.getAdmittedAt());
    result.setDischargedAt(value.getDischargedAt());
    return result;
  }
}
