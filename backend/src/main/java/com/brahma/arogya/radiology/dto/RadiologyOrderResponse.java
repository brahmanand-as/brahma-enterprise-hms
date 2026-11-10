package com.brahma.arogya.radiology.dto;

import java.math.*;
import java.time.*;

@lombok.Getter()
@lombok.Setter()
@lombok.NoArgsConstructor()
public class RadiologyOrderResponse
    implements com.brahma.arogya.common.scope.PatientScopedResponse {

  private Long id;

  private String orderNo;

  private Long encounterId;

  private Long patientId;

  private String studyName;

  private String status;

  private String report;

  private LocalDateTime createdAt;

  @Override
  public Long scopePatientId() {
    return patientId;
  }
}
