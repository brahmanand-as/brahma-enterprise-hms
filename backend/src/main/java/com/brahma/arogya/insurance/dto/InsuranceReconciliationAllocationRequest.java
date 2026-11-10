package com.brahma.arogya.insurance.dto;

import java.math.*;
import java.time.*;
import java.util.*;

public record InsuranceReconciliationAllocationRequest(
    @jakarta.validation.constraints.Positive() Long invoiceId, BigDecimal amount) {}
