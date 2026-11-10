package com.brahma.arogya.insurance.dto;

import java.math.*;
import java.time.*;
import java.util.*;

public record InsuranceReconciliationEnhancementRequest(BigDecimal amount, String notes) {}
