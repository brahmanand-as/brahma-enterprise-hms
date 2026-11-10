package com.brahma.arogya.operations.dto;

import java.util.*;

public record IntegratedOperationsAdmitRequest(
    @jakarta.validation.constraints.Positive() Long patientId,
    @jakarta.validation.constraints.Positive() Long encounterId,
    String ward,
    String bedNo) {}
