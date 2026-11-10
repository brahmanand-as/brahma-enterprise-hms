package com.brahma.arogya.hr.dto;

import java.math.*;
import java.time.*;
import java.util.*;

public record StaffingShiftRequest(
    @jakarta.validation.constraints.Positive() Long employeeId,
    LocalDate shiftDate,
    LocalTime startsAt,
    LocalTime endsAt,
    LocalDate endsOn) {}
