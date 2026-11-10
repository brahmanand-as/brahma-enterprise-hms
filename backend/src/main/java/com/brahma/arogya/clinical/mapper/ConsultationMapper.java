package com.brahma.arogya.clinical.mapper;

import com.brahma.arogya.clinical.dto.ConsultationRequest;
import com.brahma.arogya.clinical.dto.ConsultationResponse;
import com.brahma.arogya.clinical.entity.Consultation;

public final class ConsultationMapper {

  public static Consultation toEntity(ConsultationRequest value) {
    if (value == null) return null;
    var result = new Consultation();
    result.setId(value.getId());
    result.setEncounterId(value.getEncounterId());
    result.setHistory(value.getHistory());
    result.setExamination(value.getExamination());
    result.setDiagnosis(value.getDiagnosis());
    result.setAdvice(value.getAdvice());
    result.setFollowUpDate(value.getFollowUpDate());
    result.setCreatedAt(value.getCreatedAt());
    return result;
  }

  public static ConsultationResponse toResponse(Consultation value) {
    if (value == null) return null;
    var result = new ConsultationResponse();
    result.setId(value.getId());
    result.setEncounterId(value.getEncounterId());
    result.setHistory(value.getHistory());
    result.setExamination(value.getExamination());
    result.setDiagnosis(value.getDiagnosis());
    result.setAdvice(value.getAdvice());
    result.setFollowUpDate(value.getFollowUpDate());
    result.setCreatedAt(value.getCreatedAt());
    return result;
  }
}
