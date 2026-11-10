package com.brahma.arogya.operations.dto;

import java.math.BigDecimal;
import java.util.*;

public record IntegratedOperationsPaymentRequest(
    BigDecimal amount,
    @jakarta.validation.constraints.NotBlank() String method,
    String referenceNo,
    Boolean verified) {}
