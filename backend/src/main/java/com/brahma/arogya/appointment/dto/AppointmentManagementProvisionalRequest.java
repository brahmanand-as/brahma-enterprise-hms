package com.brahma.arogya.appointment.dto;

import java.util.*;

public record AppointmentManagementProvisionalRequest(
    @jakarta.validation.constraints.Positive() Long patientId, String doctorName, String reason) {}
