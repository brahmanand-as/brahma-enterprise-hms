package com.brahma.arogya.hr.dto;

import java.math.*;
import java.time.*;
import java.util.*;

public record StaffingReviewRequest(@jakarta.validation.constraints.NotBlank() String status) {}
