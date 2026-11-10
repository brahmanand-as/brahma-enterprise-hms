package com.brahma.arogya.laboratory.entity;

import jakarta.persistence.*;
import java.math.*;
import java.time.*;
import lombok.*;

@Entity
@Table(name = "lab_orders")
@Getter
@Setter
@NoArgsConstructor
public class LabOrder {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String orderNo;

  private Long encounterId;

  private Long patientId;

  private String status;

  private String orderedBy;

  private LocalDateTime orderedAt;
}
