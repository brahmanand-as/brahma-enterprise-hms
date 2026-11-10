package com.brahma.arogya.laboratory.dto;

import java.math.*;
import java.time.*;

@lombok.Getter()
@lombok.Setter()
@lombok.NoArgsConstructor()
public class LabOrderRequest {

  @jakarta.validation.constraints.Positive() private Long id;

  private String orderNo;

  @jakarta.validation.constraints.Positive() private Long encounterId;

  @jakarta.validation.constraints.Positive() private Long patientId;

  private String status;

  private String orderedBy;

  private LocalDateTime orderedAt;
}
