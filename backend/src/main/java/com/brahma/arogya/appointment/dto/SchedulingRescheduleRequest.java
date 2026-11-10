package com.brahma.arogya.appointment.dto;

import java.time.*;
import java.util.*;

public record SchedulingRescheduleRequest(
    @jakarta.validation.constraints.Positive() Long slotId, String reason) {}
