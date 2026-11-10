package com.brahma.arogya.clinical.entity;

import jakarta.persistence.*;
import java.math.*;
import java.time.*;
import lombok.*;

@Entity
@Table(name = "consultations")
@Getter
@Setter
@NoArgsConstructor
public class Consultation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long encounterId;

  private String history;

  private String examination;

  private String diagnosis;

  private String advice;

  private LocalDate followUpDate;

  private LocalDateTime createdAt;
}
