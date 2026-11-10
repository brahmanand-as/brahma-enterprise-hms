package com.brahma.arogya.patient.mapper;

import com.brahma.arogya.patient.dto.PatientRequest;
import com.brahma.arogya.patient.dto.PatientResponse;
import com.brahma.arogya.patient.entity.Patient;

public final class PatientMapper {

  public static Patient toEntity(PatientRequest value) {
    if (value == null) return null;
    var result = new Patient();
    result.setId(value.getId());
    result.setUhid(value.getUhid());
    result.setFirstName(value.getFirstName());
    result.setLastName(value.getLastName());
    result.setGender(value.getGender());
    result.setDob(value.getDob());
    result.setMobile(value.getMobile());
    result.setBloodGroup(value.getBloodGroup());
    result.setAddress(value.getAddress());
    return result;
  }

  public static PatientResponse toResponse(Patient value) {
    if (value == null) return null;
    var result = new PatientResponse();
    result.setId(value.getId());
    result.setUhid(value.getUhid());
    result.setFirstName(value.getFirstName());
    result.setLastName(value.getLastName());
    result.setGender(value.getGender());
    result.setDob(value.getDob());
    result.setMobile(value.getMobile());
    result.setBloodGroup(value.getBloodGroup());
    result.setAddress(value.getAddress());
    return result;
  }
}
