package com.brahma.arogya.billing.dto;

import java.math.*;
import java.util.*;

public record FinancialRefundRequest(
    BigDecimal amount,
    @jakarta.validation.constraints.NotBlank() String method,
    String referenceNo,
    String reason) {}
