package com.brahma.arogya.hr.dto;

import java.util.*;

public record StaffPortalNewStaffRequest(
    @jakarta.validation.constraints.NotBlank()
        @jakarta.validation.constraints.Pattern(
            regexp = "[A-Za-z0-9._-]{3,80}",
            message = "Use 3–80 letters, numbers, dots, underscores or hyphens")
        String username,
    @jakarta.validation.constraints.NotBlank()
        @jakarta.validation.constraints.Size(
            min = 12,
            message = "Password must contain at least 12 characters")
        String password,
    String role,
    @jakarta.validation.constraints.Positive() Long employeeId) {}
