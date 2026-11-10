package com.brahma.arogya.operations.dto;

import java.time.*;
import java.util.*;

public record EnterpriseOperationsShiftRequest(
    @jakarta.validation.constraints.Positive() Long employeeId,
    LocalDate shiftDate,
    LocalTime startsAt,
    LocalTime endsAt) {}
