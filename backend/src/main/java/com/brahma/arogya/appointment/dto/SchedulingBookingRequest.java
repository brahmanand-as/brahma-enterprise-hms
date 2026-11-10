package com.brahma.arogya.appointment.dto;

import java.time.*;
import java.util.*;

public record SchedulingBookingRequest(
    @jakarta.validation.constraints.Positive() Long patientId,
    @jakarta.validation.constraints.Positive() @jakarta.validation.constraints.NotNull()
        Long slotId,
    String reason) {}
