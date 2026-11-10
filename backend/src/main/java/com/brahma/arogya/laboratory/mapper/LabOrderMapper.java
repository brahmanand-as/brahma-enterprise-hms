package com.brahma.arogya.laboratory.mapper;

import com.brahma.arogya.laboratory.dto.LabOrderRequest;
import com.brahma.arogya.laboratory.dto.LabOrderResponse;
import com.brahma.arogya.laboratory.entity.LabOrder;

public final class LabOrderMapper {

  public static LabOrder toEntity(LabOrderRequest value) {
    if (value == null) return null;
    var result = new LabOrder();
    result.setId(value.getId());
    result.setOrderNo(value.getOrderNo());
    result.setEncounterId(value.getEncounterId());
    result.setPatientId(value.getPatientId());
    result.setStatus(value.getStatus());
    result.setOrderedBy(value.getOrderedBy());
    result.setOrderedAt(value.getOrderedAt());
    return result;
  }

  public static LabOrderResponse toResponse(LabOrder value) {
    if (value == null) return null;
    var result = new LabOrderResponse();
    result.setId(value.getId());
    result.setOrderNo(value.getOrderNo());
    result.setEncounterId(value.getEncounterId());
    result.setPatientId(value.getPatientId());
    result.setStatus(value.getStatus());
    result.setOrderedBy(value.getOrderedBy());
    result.setOrderedAt(value.getOrderedAt());
    return result;
  }
}
