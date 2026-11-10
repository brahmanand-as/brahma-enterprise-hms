package com.brahma.arogya.hr.dto;

import java.util.*;

public record StaffPortalLeaveFormRequest(
    @jakarta.validation.constraints.NotBlank() String leaveType,
    String startDate,
    String endDate,
    String reason) {}
