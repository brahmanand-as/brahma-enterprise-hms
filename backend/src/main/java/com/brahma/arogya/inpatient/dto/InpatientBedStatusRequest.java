package com.brahma.arogya.inpatient.dto;

import java.math.*;
import java.time.*;
import java.util.*;

public record InpatientBedStatusRequest(
    @jakarta.validation.constraints.NotBlank() String status,
    @jakarta.validation.constraints.Positive() Long patientId,
    String reason) {}
