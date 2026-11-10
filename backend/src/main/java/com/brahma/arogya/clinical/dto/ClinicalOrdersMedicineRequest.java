package com.brahma.arogya.clinical.dto;

import java.util.*;

public record ClinicalOrdersMedicineRequest(
    @jakarta.validation.constraints.NotBlank() String medicineName,
    @jakarta.validation.constraints.NotBlank() String dose,
    @jakarta.validation.constraints.NotBlank() String frequency,
    @jakarta.validation.constraints.NotBlank() String duration,
    String instructions) {}
