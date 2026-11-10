package com.brahma.arogya.insurance.dto;

import java.math.*;
import java.time.*;

@lombok.Getter()
@lombok.Setter()
@lombok.NoArgsConstructor()
public class InsuranceClaimRequest {

  @jakarta.validation.constraints.Positive() private Long id;

  @jakarta.validation.constraints.Positive() private Long patientId;

  @jakarta.validation.constraints.Positive() private Long encounterId;

  private String provider;

  private String policyNo;

  private BigDecimal claimAmount;

  private BigDecimal approvedAmount;

  private String status;
}
