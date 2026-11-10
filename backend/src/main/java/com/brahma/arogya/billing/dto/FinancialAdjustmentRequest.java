package com.brahma.arogya.billing.dto;

import java.math.*;
import java.util.*;

public record FinancialAdjustmentRequest(
    @jakarta.validation.constraints.NotBlank() String type, BigDecimal amount, String reason) {}
