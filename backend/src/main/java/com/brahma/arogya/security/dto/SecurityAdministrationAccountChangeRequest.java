package com.brahma.arogya.security.dto;

import java.util.*;

public record SecurityAdministrationAccountChangeRequest(
    String role, Boolean enabled, String password) {}
