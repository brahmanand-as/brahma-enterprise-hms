package com.brahma.arogya.inpatient.dto;

import java.math.*;
import java.time.*;

@lombok.Getter()
@lombok.Setter()
@lombok.NoArgsConstructor()
public class AdmissionRequest {

  @jakarta.validation.constraints.Positive() private Long id;

  private String admissionNo;

  @jakarta.validation.constraints.Positive() private Long encounterId;

  @jakarta.validation.constraints.Positive() private Long patientId;

  private String ward;

  private String bedNo;

  private String status;

  private LocalDateTime admittedAt;

  private LocalDateTime dischargedAt;
}
