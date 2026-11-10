package com.brahma.arogya.hr.dto;

import java.math.*;
import java.time.*;
import java.util.*;

public record StaffingHeadRequest(
    String department, @jakarta.validation.constraints.NotBlank() String username) {}
