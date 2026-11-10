package com.brahma.arogya.radiology.dto;

import java.math.*;
import java.time.*;

@lombok.Getter()
@lombok.Setter()
@lombok.NoArgsConstructor()
public class RadiologyOrderRequest {

  @jakarta.validation.constraints.Positive() private Long id;

  private String orderNo;

  @jakarta.validation.constraints.Positive() private Long encounterId;

  @jakarta.validation.constraints.Positive() private Long patientId;

  private String studyName;

  private String status;

  private String report;

  private LocalDateTime createdAt;
}
