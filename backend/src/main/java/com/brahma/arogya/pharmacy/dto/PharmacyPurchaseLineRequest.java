package com.brahma.arogya.pharmacy.dto;

import java.math.*;
import java.time.*;
import java.util.*;

public record PharmacyPurchaseLineRequest(
    @jakarta.validation.constraints.Positive() Long inventoryItemId,
    @jakarta.validation.constraints.Positive() @jakarta.validation.constraints.NotNull()
        BigDecimal quantity,
    @jakarta.validation.constraints.PositiveOrZero() BigDecimal unitCost) {}
