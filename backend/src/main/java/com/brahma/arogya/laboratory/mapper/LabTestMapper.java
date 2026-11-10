package com.brahma.arogya.laboratory.mapper;

import com.brahma.arogya.laboratory.dto.LabTestRequest;
import com.brahma.arogya.laboratory.dto.LabTestResponse;
import com.brahma.arogya.laboratory.entity.LabTest;

public final class LabTestMapper {

  public static LabTest toEntity(LabTestRequest value) {
    if (value == null) return null;
    var result = new LabTest();
    result.setId(value.getId());
    result.setCode(value.getCode());
    result.setName(value.getName());
    result.setSampleType(value.getSampleType());
    result.setDefaultProcessingType(value.getDefaultProcessingType());
    result.setPrice(value.getPrice());
    result.setResultUnit(value.getResultUnit());
    result.setReferenceRange(value.getReferenceRange());
    result.setActive(value.getActive());
    return result;
  }

  public static LabTestResponse toResponse(LabTest value) {
    if (value == null) return null;
    var result = new LabTestResponse();
    result.setId(value.getId());
    result.setCode(value.getCode());
    result.setName(value.getName());
    result.setSampleType(value.getSampleType());
    result.setDefaultProcessingType(value.getDefaultProcessingType());
    result.setPrice(value.getPrice());
    result.setResultUnit(value.getResultUnit());
    result.setReferenceRange(value.getReferenceRange());
    result.setActive(value.getActive());
    return result;
  }
}
