package com.brahma.arogya.billing.dto;

import java.math.*;
import java.time.*;

@lombok.Getter()
@lombok.Setter()
@lombok.NoArgsConstructor()
public class InvoiceRequest {

  @jakarta.validation.constraints.Positive() private Long id;

  private String invoiceNo;

  @jakarta.validation.constraints.Positive() private Long patientId;

  @jakarta.validation.constraints.Positive() private Long encounterId;

  private BigDecimal total;

  private BigDecimal paid;

  private String status;

  private LocalDateTime createdAt;
}
