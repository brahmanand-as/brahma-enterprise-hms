package com.brahma.arogya.clinical.dto;

import java.util.*;

public record ClinicalOrdersPrescriptionRequest(
    @jakarta.validation.constraints.Positive() @jakarta.validation.constraints.NotNull()
        Long encounterId,
    @jakarta.validation.Valid() @jakarta.validation.constraints.NotEmpty()
        List<ClinicalOrdersMedicineRequest> items) {}
