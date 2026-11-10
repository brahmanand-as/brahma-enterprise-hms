package com.brahma.arogya.pharmacy.dto;

import java.math.*;
import java.time.*;
import java.util.*;

public record PharmacyStockActionRequest(
    @jakarta.validation.constraints.Positive() @jakarta.validation.constraints.NotNull()
        BigDecimal quantity,
    String reason,
    String ward,
    @jakarta.validation.constraints.Positive() Long patientId,
    @jakarta.validation.constraints.Positive() Long admissionId) {}
