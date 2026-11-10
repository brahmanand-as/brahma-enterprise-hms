package com.brahma.arogya.pharmacy.dto;

import java.math.*;
import java.time.*;
import java.util.*;

public record PharmacyReceiptRequest(
    @jakarta.validation.constraints.Positive() Long purchaseLineId,
    String batchNo,
    LocalDate expiresOn,
    @jakarta.validation.constraints.Positive() @jakarta.validation.constraints.NotNull()
        BigDecimal quantity,
    @jakarta.validation.constraints.PositiveOrZero() BigDecimal salePrice) {}
