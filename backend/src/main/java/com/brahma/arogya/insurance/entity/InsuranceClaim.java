package com.brahma.arogya.insurance.entity;

import jakarta.persistence.*;
import java.math.*;
import java.time.*;
import lombok.*;

@Entity
@Table(name = "insurance_claims")
@Getter
@Setter
@NoArgsConstructor
public class InsuranceClaim {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long patientId;

  private Long encounterId;

  private String provider;

  private String policyNo;

  private BigDecimal claimAmount;

  private BigDecimal approvedAmount;

  private String status;
}
