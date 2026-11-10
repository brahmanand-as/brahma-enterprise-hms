package com.brahma.arogya.inpatient.dto;

import java.math.*;
import java.time.*;

@lombok.Getter()
@lombok.Setter()
@lombok.NoArgsConstructor()
public class AdmissionResponse implements com.brahma.arogya.common.scope.PatientScopedResponse {

  private Long id;

  private String admissionNo;

  private Long encounterId;

  private Long patientId;

  private String ward;

  private String bedNo;

  private String status;

  private LocalDateTime admittedAt;

  private LocalDateTime dischargedAt;

  @Override
  public Long scopePatientId() {
    return patientId;
  }
}
