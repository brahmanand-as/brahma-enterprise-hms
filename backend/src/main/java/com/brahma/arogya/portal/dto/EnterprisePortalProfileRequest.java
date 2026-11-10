package com.brahma.arogya.portal.dto;

import java.util.*;

public record EnterprisePortalProfileRequest(
    @jakarta.validation.constraints.NotBlank()
        @jakarta.validation.constraints.Pattern(
            regexp = "[0-9]{10}",
            message = "Enter a ten-digit mobile number")
        String mobile,
    String address,
    String bloodGroup) {}
