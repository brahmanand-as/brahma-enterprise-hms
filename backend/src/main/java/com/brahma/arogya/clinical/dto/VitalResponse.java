package com.brahma.arogya.clinical.dto;

import java.math.*;
import java.time.*;

@lombok.Getter()
@lombok.Setter()
@lombok.NoArgsConstructor()
public class VitalResponse implements com.brahma.arogya.common.scope.EncounterScopedResponse {

  private Long id;

  private Long encounterId;

  private BigDecimal temperature;

  private Integer pulse;

  private Integer systolic;

  private Integer diastolic;

  private BigDecimal spo2;

  private BigDecimal weightKg;

  private BigDecimal heightCm;

  private LocalDateTime recordedAt;

  @Override
  public Long scopeEncounterId() {
    return encounterId;
  }
}
