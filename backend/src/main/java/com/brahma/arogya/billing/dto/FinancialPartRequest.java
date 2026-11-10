package com.brahma.arogya.billing.dto;

import java.math.*;
import java.util.*;

public record FinancialPartRequest(
    BigDecimal amount,
    @jakarta.validation.constraints.NotBlank() String method,
    String referenceNo,
    Boolean verified) {}
