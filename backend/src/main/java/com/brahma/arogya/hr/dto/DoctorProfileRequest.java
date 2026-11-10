package com.brahma.arogya.hr.dto;

import java.util.*;

public record DoctorProfileRequest(
    @jakarta.validation.constraints.Positive() Long employeeId,
    String specialty,
    String qualification,
    Integer experienceYears,
    String registrationNumber,
    java.math.BigDecimal consultationFee,
    String photoUrl,
    String biography,
    String availabilityNotes) {}
