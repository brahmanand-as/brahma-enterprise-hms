package com.brahma.arogya.clinical.dto;

import java.math.*;
import java.time.*;

@lombok.Getter()
@lombok.Setter()
@lombok.NoArgsConstructor()
public class VitalRequest {

  @jakarta.validation.constraints.Positive() private Long id;

  @jakarta.validation.constraints.Positive() private Long encounterId;

  private BigDecimal temperature;

  @jakarta.validation.constraints.Positive() private Integer pulse;

  @jakarta.validation.constraints.Positive() private Integer systolic;

  @jakarta.validation.constraints.Positive() private Integer diastolic;

  @jakarta.validation.constraints.PositiveOrZero()
  @jakarta.validation.constraints.DecimalMax("100")
  private BigDecimal spo2;

  @jakarta.validation.constraints.Positive() private BigDecimal weightKg;

  @jakarta.validation.constraints.Positive() private BigDecimal heightCm;

  private LocalDateTime recordedAt;
}
