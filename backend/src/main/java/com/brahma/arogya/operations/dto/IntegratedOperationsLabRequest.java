package com.brahma.arogya.operations.dto;

import java.util.*;

public record IntegratedOperationsLabRequest(
    @jakarta.validation.constraints.Positive() Long encounterId,
    String orderedBy,
    @jakarta.validation.Valid() @jakarta.validation.constraints.NotEmpty()
        List<IntegratedOperationsTestRequest> tests) {}
