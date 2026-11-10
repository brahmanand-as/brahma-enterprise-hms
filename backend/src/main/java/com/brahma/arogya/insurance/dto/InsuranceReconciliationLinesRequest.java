package com.brahma.arogya.insurance.dto;

import java.math.*;
import java.time.*;
import java.util.*;

public record InsuranceReconciliationLinesRequest(
    @jakarta.validation.Valid() @jakarta.validation.constraints.NotEmpty()
        List<InsuranceReconciliationLineRequest> lines) {}
