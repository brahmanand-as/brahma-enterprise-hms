package com.brahma.arogya.portal.dto;

import java.util.*;

public record EnterprisePortalTicketRequest(
    String subject,
    String description,
    @jakarta.validation.constraints.NotBlank() String priority) {}
