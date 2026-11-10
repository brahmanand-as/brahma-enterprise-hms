package com.brahma.arogya.clinical.dto;

import java.math.*;
import java.time.*;

@lombok.Getter()
@lombok.Setter()
@lombok.NoArgsConstructor()
public class ConsultationRequest {

  @jakarta.validation.constraints.Positive() private Long id;

  @jakarta.validation.constraints.Positive() private Long encounterId;

  private String history;

  private String examination;

  @jakarta.validation.constraints.NotBlank() private String diagnosis;

  private String advice;

  private LocalDate followUpDate;

  private LocalDateTime createdAt;
}
