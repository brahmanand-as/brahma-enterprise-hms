package com.brahma.arogya.pharmacy.dto;

import java.math.*;
import java.time.*;
import java.util.*;

public record PharmacyConsumableReturnRequest(
    @jakarta.validation.constraints.Positive() Long movementId,
    @jakarta.validation.constraints.Positive() @jakarta.validation.constraints.NotNull()
        BigDecimal quantity,
    String reason) {}
