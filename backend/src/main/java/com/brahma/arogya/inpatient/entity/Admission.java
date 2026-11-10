package com.brahma.arogya.inpatient.entity;

import jakarta.persistence.*;
import java.math.*;
import java.time.*;
import lombok.*;

@Entity
@Table(name = "admissions")
@Getter
@Setter
@NoArgsConstructor
public class Admission {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String admissionNo;

  private Long encounterId;

  private Long patientId;

  private String ward;

  private String bedNo;

  private String status;

  private LocalDateTime admittedAt;

  private LocalDateTime dischargedAt;
}
