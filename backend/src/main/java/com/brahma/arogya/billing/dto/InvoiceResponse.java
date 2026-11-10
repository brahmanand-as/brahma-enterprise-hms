package com.brahma.arogya.billing.dto;

import java.math.*;
import java.time.*;

@lombok.Getter()
@lombok.Setter()
@lombok.NoArgsConstructor()
public class InvoiceResponse {

  private Long id;

  private String invoiceNo;

  private Long patientId;

  private Long encounterId;

  private BigDecimal total;

  private BigDecimal paid;

  private String status;

  private LocalDateTime createdAt;
}
