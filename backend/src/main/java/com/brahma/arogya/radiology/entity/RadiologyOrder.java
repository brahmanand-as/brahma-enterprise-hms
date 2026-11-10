package com.brahma.arogya.radiology.entity;

import jakarta.persistence.*;
import java.math.*;
import java.time.*;
import lombok.*;

@Entity
@Table(name = "radiology_orders")
@Getter
@Setter
@NoArgsConstructor
public class RadiologyOrder {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String orderNo;

  private Long encounterId;

  private Long patientId;

  private String studyName;

  private String status;

  private String report;

  private LocalDateTime createdAt;
}
