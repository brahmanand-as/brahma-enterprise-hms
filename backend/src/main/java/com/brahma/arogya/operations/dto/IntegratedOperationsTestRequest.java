package com.brahma.arogya.operations.dto;

import java.util.*;

public record IntegratedOperationsTestRequest(
    String testCode,
    String processingType,
    @jakarta.validation.constraints.Positive() Long externalLabId) {}
