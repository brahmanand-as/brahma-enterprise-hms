package com.brahma.arogya.security.dto;

import java.util.*;

public record CareAssignmentRequest(
    @jakarta.validation.constraints.Positive() @jakarta.validation.constraints.NotNull()
        Long patientId,
    @jakarta.validation.constraints.NotBlank() String username) {}
