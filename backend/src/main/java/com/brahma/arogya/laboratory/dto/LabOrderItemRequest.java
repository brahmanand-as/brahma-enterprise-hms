package com.brahma.arogya.laboratory.dto;

import java.math.*;
import java.time.*;

@lombok.Getter()
@lombok.Setter()
@lombok.NoArgsConstructor()
public class LabOrderItemRequest {

  @jakarta.validation.constraints.Positive() private Long id;

  @jakarta.validation.constraints.Positive() private Long labOrderId;

  private String testCode;

  private String testName;

  private String processingType;

  @jakarta.validation.constraints.Positive() private Long externalLabId;

  private String status;

  private String resultValue;

  private String resultUnit;

  private String referenceRange;

  private String verifiedBy;

  private LocalDateTime verifiedAt;
}
