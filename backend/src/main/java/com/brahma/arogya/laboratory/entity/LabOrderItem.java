package com.brahma.arogya.laboratory.entity;

import jakarta.persistence.*;
import java.math.*;
import java.time.*;
import lombok.*;

@Entity
@Table(name = "lab_order_items")
@Getter
@Setter
@NoArgsConstructor
public class LabOrderItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long labOrderId;

  private String testCode;

  private String testName;

  private String processingType;

  private Long externalLabId;

  private String status;

  private String resultValue;

  private String resultUnit;

  private String referenceRange;

  private String verifiedBy;

  private LocalDateTime verifiedAt;
}
