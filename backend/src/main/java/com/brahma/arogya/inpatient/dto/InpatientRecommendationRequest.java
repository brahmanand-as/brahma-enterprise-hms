package com.brahma.arogya.inpatient.dto;

import java.math.*;
import java.time.*;
import java.util.*;

public record InpatientRecommendationRequest(
    @jakarta.validation.constraints.Positive() Long encounterId, String wardType, String reason) {}
