package com.brahma.arogya.clinical.dto;

import java.util.*;

public record ClinicalOrdersRadiologyRequest(
    @jakarta.validation.constraints.Positive() Long encounterId, String studyName) {}
