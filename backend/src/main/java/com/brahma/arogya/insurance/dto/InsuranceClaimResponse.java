package com.brahma.arogya.insurance.dto;

import java.math.*;
import java.time.*;

@lombok.Getter()
@lombok.Setter()
@lombok.NoArgsConstructor()
public class InsuranceClaimResponse {

  private Long id;

  private Long patientId;

  private Long encounterId;

  private String provider;

  private String policyNo;

  private BigDecimal claimAmount;

  private BigDecimal approvedAmount;

  private String status;
}
