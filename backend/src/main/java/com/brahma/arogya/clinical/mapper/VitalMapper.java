package com.brahma.arogya.clinical.mapper;

import com.brahma.arogya.clinical.dto.VitalRequest;
import com.brahma.arogya.clinical.dto.VitalResponse;
import com.brahma.arogya.clinical.entity.Vital;

public final class VitalMapper {

  public static Vital toEntity(VitalRequest value) {
    if (value == null) return null;
    var result = new Vital();
    result.setId(value.getId());
    result.setEncounterId(value.getEncounterId());
    result.setTemperature(value.getTemperature());
    result.setPulse(value.getPulse());
    result.setSystolic(value.getSystolic());
    result.setDiastolic(value.getDiastolic());
    result.setSpo2(value.getSpo2());
    result.setWeightKg(value.getWeightKg());
    result.setHeightCm(value.getHeightCm());
    result.setRecordedAt(value.getRecordedAt());
    return result;
  }

  public static VitalResponse toResponse(Vital value) {
    if (value == null) return null;
    var result = new VitalResponse();
    result.setId(value.getId());
    result.setEncounterId(value.getEncounterId());
    result.setTemperature(value.getTemperature());
    result.setPulse(value.getPulse());
    result.setSystolic(value.getSystolic());
    result.setDiastolic(value.getDiastolic());
    result.setSpo2(value.getSpo2());
    result.setWeightKg(value.getWeightKg());
    result.setHeightCm(value.getHeightCm());
    result.setRecordedAt(value.getRecordedAt());
    return result;
  }
}
