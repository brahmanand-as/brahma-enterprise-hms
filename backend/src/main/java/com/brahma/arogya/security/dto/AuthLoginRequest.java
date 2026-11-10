package com.brahma.arogya.security.dto;

import java.util.*;

public record AuthLoginRequest(
    @jakarta.validation.constraints.NotBlank() String username,
    @jakarta.validation.constraints.NotBlank() String password) {}
