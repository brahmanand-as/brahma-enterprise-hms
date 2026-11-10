package com.brahma.arogya.insurance.dto;

import java.math.*;
import java.time.*;
import java.util.*;

public record InsuranceReconciliationDecisionRequest(
    @jakarta.validation.constraints.NotBlank() String status, BigDecimal amount, String notes) {}
