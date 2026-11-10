package com.brahma.arogya.inpatient.dto;

import java.time.*;
import java.util.*;

public record WardAdministrationRequest(
    @jakarta.validation.constraints.NotBlank() String status, String notes) {}
