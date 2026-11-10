package com.brahma.arogya.portal.dto;

import java.util.*;

public record EnterprisePortalRegistrationRequest(
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
    @jakarta.validation.constraints.NotBlank() String firstName,
    String lastName,
    @jakarta.validation.constraints.NotBlank()
        @jakarta.validation.constraints.Pattern(
            regexp = "[0-9]{10}",
            message = "Enter a ten-digit mobile number")
        String mobile) {}
