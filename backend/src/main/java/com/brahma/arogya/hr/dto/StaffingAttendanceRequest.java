package com.brahma.arogya.hr.dto;

import java.math.*;
import java.time.*;
import java.util.*;

public record StaffingAttendanceRequest(
    @jakarta.validation.constraints.Positive() Long employeeId,
    LocalDate workDate,
    @jakarta.validation.constraints.NotBlank() String status,
    @jakarta.validation.constraints.PositiveOrZero() BigDecimal hoursWorked) {}
