package com.brahma.arogya.insurance.dto;

import java.math.*;
import java.time.*;
import java.util.*;

public record InsuranceReconciliationLineRequest(
    String description,
    BigDecimal amount,
    @jakarta.validation.constraints.PositiveOrZero() BigDecimal eligibleAmount) {}
