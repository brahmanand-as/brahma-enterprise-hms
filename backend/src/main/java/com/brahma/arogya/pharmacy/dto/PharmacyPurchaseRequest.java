package com.brahma.arogya.pharmacy.dto;

import java.math.*;
import java.time.*;
import java.util.*;

public record PharmacyPurchaseRequest(
    @jakarta.validation.constraints.Positive() Long supplierId,
    @jakarta.validation.Valid() @jakarta.validation.constraints.NotEmpty()
        List<PharmacyPurchaseLineRequest> lines) {}
