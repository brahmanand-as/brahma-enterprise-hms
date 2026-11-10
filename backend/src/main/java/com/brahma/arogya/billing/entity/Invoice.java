package com.brahma.arogya.billing.entity;

import jakarta.persistence.*;
import java.math.*;
import java.time.*;
import lombok.*;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
public class Invoice {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String invoiceNo;

  private Long patientId;

  private Long encounterId;

  private BigDecimal total;

  private BigDecimal paid;

  private String status;

  private LocalDateTime createdAt;
}
