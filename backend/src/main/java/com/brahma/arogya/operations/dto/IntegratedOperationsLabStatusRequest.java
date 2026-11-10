package com.brahma.arogya.operations.dto;

import java.util.*;

public record IntegratedOperationsLabStatusRequest(
    @jakarta.validation.constraints.NotBlank() String status,
    String resultValue,
    String resultUnit,
    String referenceRange,
    String verifiedBy) {}
