package com.brahma.arogya.billing.dto;

import java.math.*;
import java.util.*;

public record FinancialSplitRequest(
    @jakarta.validation.Valid() @jakarta.validation.constraints.NotEmpty()
        List<FinancialPartRequest> parts) {}
