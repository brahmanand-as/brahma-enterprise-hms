package com.brahma.arogya.hr.dto;

import java.util.*;

public record StaffPortalSalaryFormRequest(
    @jakarta.validation.constraints.Positive() Long employeeId,
    String salaryMonth,
    @jakarta.validation.constraints.PositiveOrZero() java.math.BigDecimal basic,
    @jakarta.validation.constraints.PositiveOrZero() java.math.BigDecimal allowances,
    @jakarta.validation.constraints.PositiveOrZero() java.math.BigDecimal deductions,
    String paymentStatus) {}
