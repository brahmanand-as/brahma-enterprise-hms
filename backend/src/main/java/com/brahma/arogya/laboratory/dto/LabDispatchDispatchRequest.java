package com.brahma.arogya.laboratory.dto;

import java.util.*;

public record LabDispatchDispatchRequest(
    @jakarta.validation.constraints.Positive() Long labOrderItemId,
    String sampleId,
    String courierDetails,
    String trackingNumber) {}
