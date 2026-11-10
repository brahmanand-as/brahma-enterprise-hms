package com.brahma.arogya.operations.dto;

import java.time.*;
import java.util.*;

public record EnterpriseOperationsRadiologyTransitionRequest(
    @jakarta.validation.constraints.NotBlank() String status, String report) {}
