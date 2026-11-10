package com.brahma.arogya.insurance.dto;

import java.math.*;
import java.time.*;
import java.util.*;

public record InsuranceReconciliationSettlementRequest(
    BigDecimal amount, String referenceNo, LocalDate receivedOn) {}
