package com.brahma.arogya.inpatient.dto;

import java.math.*;
import java.time.*;
import java.util.*;

public record InpatientNoteRequest(
    @jakarta.validation.constraints.Positive() Long admissionId,
    String noteType,
    String assessment,
    String plan) {}
