package com.brahma.arogya.operations.dto;

import java.math.BigDecimal;
import java.util.*;

public record IntegratedOperationsStockRequest(
    @jakarta.validation.constraints.Positive() @jakarta.validation.constraints.NotNull()
        BigDecimal qty,
    String reason) {}
