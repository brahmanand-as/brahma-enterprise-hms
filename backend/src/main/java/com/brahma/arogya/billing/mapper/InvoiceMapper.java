package com.brahma.arogya.billing.mapper;

import com.brahma.arogya.billing.dto.InvoiceRequest;
import com.brahma.arogya.billing.dto.InvoiceResponse;
import com.brahma.arogya.billing.entity.Invoice;

public final class InvoiceMapper {

  public static Invoice toEntity(InvoiceRequest value) {
    if (value == null) return null;
    var result = new Invoice();
    result.setId(value.getId());
    result.setInvoiceNo(value.getInvoiceNo());
    result.setPatientId(value.getPatientId());
    result.setEncounterId(value.getEncounterId());
    result.setTotal(value.getTotal());
    result.setPaid(value.getPaid());
    result.setStatus(value.getStatus());
    result.setCreatedAt(value.getCreatedAt());
    return result;
  }

  public static InvoiceResponse toResponse(Invoice value) {
    if (value == null) return null;
    var result = new InvoiceResponse();
    result.setId(value.getId());
    result.setInvoiceNo(value.getInvoiceNo());
    result.setPatientId(value.getPatientId());
    result.setEncounterId(value.getEncounterId());
    result.setTotal(value.getTotal());
    result.setPaid(value.getPaid());
    result.setStatus(value.getStatus());
    result.setCreatedAt(value.getCreatedAt());
    return result;
  }
}
