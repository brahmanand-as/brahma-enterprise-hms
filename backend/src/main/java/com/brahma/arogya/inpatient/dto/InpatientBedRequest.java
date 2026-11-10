package com.brahma.arogya.inpatient.dto;

import java.math.*;
import java.time.*;
import java.util.*;

public record InpatientBedRequest(
    String ward,
    String bedNo,
    String floorName,
    String bedType,
    @jakarta.validation.constraints.PositiveOrZero() BigDecimal dailyRate) {}
