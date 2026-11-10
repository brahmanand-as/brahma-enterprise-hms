package com.brahma.arogya.operations.dto;

import java.math.BigDecimal;
import java.util.*;

public record IntegratedOperationsInvoiceLineRequest(
    String description,
    @jakarta.validation.constraints.Positive() @jakarta.validation.constraints.NotNull()
        BigDecimal qty,
    BigDecimal unitPrice,
    String itemType,
    @jakarta.validation.constraints.Positive() Long referenceId) {}
