package com.brahma.arogya.inpatient.dto;

import java.math.*;
import java.time.*;
import java.util.*;

public record InpatientMedicationRequest(
    @jakarta.validation.constraints.Positive() Long admissionId,
    @jakarta.validation.constraints.Positive() Long inventoryItemId,
    String dose,
    String route,
    LocalDateTime startsAt,
    Integer intervalHours,
    Integer doseCount,
    @jakarta.validation.constraints.Positive() @jakarta.validation.constraints.NotNull()
        BigDecimal quantityPerDose) {}
