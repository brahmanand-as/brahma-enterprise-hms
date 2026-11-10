package com.brahma.arogya.radiology.mapper;

import com.brahma.arogya.radiology.dto.RadiologyOrderRequest;
import com.brahma.arogya.radiology.dto.RadiologyOrderResponse;
import com.brahma.arogya.radiology.entity.RadiologyOrder;

public final class RadiologyOrderMapper {

  public static RadiologyOrder toEntity(RadiologyOrderRequest value) {
    if (value == null) return null;
    var result = new RadiologyOrder();
    result.setId(value.getId());
    result.setOrderNo(value.getOrderNo());
    result.setEncounterId(value.getEncounterId());
    result.setPatientId(value.getPatientId());
    result.setStudyName(value.getStudyName());
    result.setStatus(value.getStatus());
    result.setReport(value.getReport());
    result.setCreatedAt(value.getCreatedAt());
    return result;
  }

  public static RadiologyOrderResponse toResponse(RadiologyOrder value) {
    if (value == null) return null;
    var result = new RadiologyOrderResponse();
    result.setId(value.getId());
    result.setOrderNo(value.getOrderNo());
    result.setEncounterId(value.getEncounterId());
    result.setPatientId(value.getPatientId());
    result.setStudyName(value.getStudyName());
    result.setStatus(value.getStatus());
    result.setReport(value.getReport());
    result.setCreatedAt(value.getCreatedAt());
    return result;
  }
}
