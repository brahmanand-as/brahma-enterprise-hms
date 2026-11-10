package com.brahma.arogya.insurance.dto;

import java.math.BigDecimal;
import java.util.*;

public record InsuranceIntakeRequest(
    @jakarta.validation.constraints.Positive() Long admissionId,
    String provider,
    String policyNo,
    String claimType,
    BigDecimal claimAmount,
    @jakarta.validation.constraints.PositiveOrZero() BigDecimal nonPayable) {}
