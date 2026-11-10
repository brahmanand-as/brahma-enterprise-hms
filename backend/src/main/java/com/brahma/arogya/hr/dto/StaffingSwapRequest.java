package com.brahma.arogya.hr.dto;

import java.math.*;
import java.time.*;
import java.util.*;

public record StaffingSwapRequest(
    @jakarta.validation.constraints.Positive() Long rosterId,
    @jakarta.validation.constraints.Positive() Long replacementEmployeeId,
    String reason) {}
