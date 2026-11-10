package com.brahma.arogya.appointment.dto;

import java.util.*;

public record AppointmentManagementResolveRequest(
    @jakarta.validation.constraints.Positive() Long slotId, String reason) {}
