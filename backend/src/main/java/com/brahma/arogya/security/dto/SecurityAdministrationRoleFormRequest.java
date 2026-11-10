package com.brahma.arogya.security.dto;

import java.util.*;

public record SecurityAdministrationRoleFormRequest(
    String name, String label, @jakarta.validation.Valid() List<String> permissions) {}
