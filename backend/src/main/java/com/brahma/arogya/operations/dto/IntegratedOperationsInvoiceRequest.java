package com.brahma.arogya.operations.dto;

import java.util.*;

public record IntegratedOperationsInvoiceRequest(
    @jakarta.validation.constraints.Positive() Long patientId,
    @jakarta.validation.constraints.Positive() Long encounterId,
    @jakarta.validation.Valid() @jakarta.validation.constraints.NotEmpty()
        List<IntegratedOperationsInvoiceLineRequest> items,
    @jakarta.validation.constraints.Positive() Long departmentId) {

  public IntegratedOperationsInvoiceRequest(
      Long patient, Long encounter, List<IntegratedOperationsInvoiceLineRequest> items) {
    this(patient, encounter, items, null);
  }
}
