package com.brahma.arogya.portal.dto;

import java.util.*;

public record PersonalWorkspacePasswordChangeRequest(
    @jakarta.validation.constraints.NotBlank() String currentPassword,
    @jakarta.validation.constraints.NotBlank()
        @jakarta.validation.constraints.Size(
            min = 12,
            message = "Password must contain at least 12 characters")
        String newPassword) {}
