package com.brahma.arogya.clinical.mapper;

import com.brahma.arogya.clinical.dto.EncounterRequest;
import com.brahma.arogya.clinical.dto.EncounterResponse;
import com.brahma.arogya.clinical.entity.Encounter;

public final class EncounterMapper {

  public static Encounter toEntity(EncounterRequest value) {
    if (value == null) return null;
    var result = new Encounter();
    result.setId(value.getId());
    result.setDoctorEmployeeId(value.getDoctorEmployeeId());
    result.setEncounterNo(value.getEncounterNo());
    result.setPatientId(value.getPatientId());
    result.setAppointmentId(value.getAppointmentId());
    result.setDepartmentId(value.getDepartmentId());
    result.setDoctorName(value.getDoctorName());
    result.setEncounterType(value.getEncounterType());
    result.setStatus(value.getStatus());
    result.setChiefComplaint(value.getChiefComplaint());
    result.setStartedAt(value.getStartedAt());
    result.setCompletedAt(value.getCompletedAt());
    return result;
  }

  public static EncounterResponse toResponse(Encounter value) {
    if (value == null) return null;
    var result = new EncounterResponse();
    result.setId(value.getId());
    result.setDoctorEmployeeId(value.getDoctorEmployeeId());
    result.setEncounterNo(value.getEncounterNo());
    result.setPatientId(value.getPatientId());
    result.setAppointmentId(value.getAppointmentId());
    result.setDepartmentId(value.getDepartmentId());
    result.setDoctorName(value.getDoctorName());
    result.setEncounterType(value.getEncounterType());
    result.setStatus(value.getStatus());
    result.setChiefComplaint(value.getChiefComplaint());
    result.setStartedAt(value.getStartedAt());
    result.setCompletedAt(value.getCompletedAt());
    return result;
  }
}
