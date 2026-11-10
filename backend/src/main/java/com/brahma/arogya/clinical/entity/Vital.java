package com.brahma.arogya.clinical.entity;

import jakarta.persistence.*;
import java.math.*;
import java.time.*;
import lombok.*;

@Entity
@Table(name = "vitals")
@Getter
@Setter
@NoArgsConstructor
public class Vital {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
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
}
