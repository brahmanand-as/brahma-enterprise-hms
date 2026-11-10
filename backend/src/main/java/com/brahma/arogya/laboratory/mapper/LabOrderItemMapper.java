package com.brahma.arogya.laboratory.mapper;

import com.brahma.arogya.laboratory.dto.LabOrderItemRequest;
import com.brahma.arogya.laboratory.dto.LabOrderItemResponse;
import com.brahma.arogya.laboratory.entity.LabOrderItem;

public final class LabOrderItemMapper {

  public static LabOrderItem toEntity(LabOrderItemRequest value) {
    if (value == null) return null;
    var result = new LabOrderItem();
    result.setId(value.getId());
    result.setLabOrderId(value.getLabOrderId());
    result.setTestCode(value.getTestCode());
    result.setTestName(value.getTestName());
    result.setProcessingType(value.getProcessingType());
    result.setExternalLabId(value.getExternalLabId());
    result.setStatus(value.getStatus());
    result.setResultValue(value.getResultValue());
    result.setResultUnit(value.getResultUnit());
    result.setReferenceRange(value.getReferenceRange());
    result.setVerifiedBy(value.getVerifiedBy());
    result.setVerifiedAt(value.getVerifiedAt());
    return result;
  }

  public static LabOrderItemResponse toResponse(LabOrderItem value) {
    if (value == null) return null;
    var result = new LabOrderItemResponse();
    result.setId(value.getId());
    result.setLabOrderId(value.getLabOrderId());
    result.setTestCode(value.getTestCode());
    result.setTestName(value.getTestName());
    result.setProcessingType(value.getProcessingType());
    result.setExternalLabId(value.getExternalLabId());
    result.setStatus(value.getStatus());
    result.setResultValue(value.getResultValue());
    result.setResultUnit(value.getResultUnit());
    result.setReferenceRange(value.getReferenceRange());
    result.setVerifiedBy(value.getVerifiedBy());
    result.setVerifiedAt(value.getVerifiedAt());
    return result;
  }
}
