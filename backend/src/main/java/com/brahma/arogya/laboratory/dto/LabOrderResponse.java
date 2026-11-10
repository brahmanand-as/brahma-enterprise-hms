package com.brahma.arogya.laboratory.dto;

import java.math.*;
import java.time.*;

@lombok.Getter()
@lombok.Setter()
@lombok.NoArgsConstructor()
public class LabOrderResponse implements com.brahma.arogya.common.scope.PatientScopedResponse {

  private Long id;

  private String orderNo;

  private Long encounterId;

  private Long patientId;

  private String status;

  private String orderedBy;

  private LocalDateTime orderedAt;

  @Override
  public Long scopePatientId() {
    return patientId;
  }
}
