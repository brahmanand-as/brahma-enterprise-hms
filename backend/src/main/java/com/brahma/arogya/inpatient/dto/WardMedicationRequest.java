package com.brahma.arogya.inpatient.dto;

import java.time.*;
import java.util.*;

public record WardMedicationRequest(
    @jakarta.validation.constraints.Positive() Long admissionId,
    String medicineName,
    String dose,
    String route,
    LocalDateTime scheduledAt) {}
