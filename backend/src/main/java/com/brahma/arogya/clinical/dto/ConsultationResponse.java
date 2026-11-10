package com.brahma.arogya.clinical.dto;

import java.math.*;
import java.time.*;

@lombok.Getter()
@lombok.Setter()
@lombok.NoArgsConstructor()
public class ConsultationResponse
    implements com.brahma.arogya.common.scope.EncounterScopedResponse {

  private Long id;

  private Long encounterId;

  private String history;

  private String examination;

  private String diagnosis;

  private String advice;

  private LocalDate followUpDate;

  private LocalDateTime createdAt;

  @Override
  public Long scopeEncounterId() {
    return encounterId;
  }
}
