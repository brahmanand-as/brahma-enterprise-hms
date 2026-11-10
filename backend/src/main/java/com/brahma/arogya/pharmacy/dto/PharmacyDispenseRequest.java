package com.brahma.arogya.pharmacy.dto;

import java.math.*;
import java.time.*;
import java.util.*;

public record PharmacyDispenseRequest(
    @jakarta.validation.constraints.Positive() @jakarta.validation.constraints.NotNull()
        Long patientId,
    @jakarta.validation.constraints.Positive() Long prescriptionId,
    @jakarta.validation.constraints.Positive() Long medicationOrderId,
    @jakarta.validation.constraints.Positive() @jakarta.validation.constraints.NotNull()
        Long inventoryItemId,
    @jakarta.validation.constraints.Positive()
        @jakarta.validation.constraints.NotNull()
        @jakarta.validation.constraints.NotNull()
        BigDecimal quantity) {}
