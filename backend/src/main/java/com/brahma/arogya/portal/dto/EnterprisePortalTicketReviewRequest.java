package com.brahma.arogya.portal.dto;

import java.util.*;

public record EnterprisePortalTicketReviewRequest(
    @jakarta.validation.constraints.NotBlank() String status,
    String assignedTo,
    String resolution,
    String department) {}
