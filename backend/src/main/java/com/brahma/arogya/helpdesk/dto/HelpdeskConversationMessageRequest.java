package com.brahma.arogya.helpdesk.dto;

import java.util.*;

public record HelpdeskConversationMessageRequest(
    @jakarta.validation.constraints.NotBlank() String message) {}
