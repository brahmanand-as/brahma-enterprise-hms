package com.brahma.arogya.operations.dto;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public record EnterpriseOperationsClaimTransitionRequest(
    @jakarta.validation.constraints.NotBlank() String status, BigDecimal amount, String notes) {}
