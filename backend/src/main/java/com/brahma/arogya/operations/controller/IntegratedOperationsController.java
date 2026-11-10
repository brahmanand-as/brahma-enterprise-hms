package com.brahma.arogya.operations.controller;

import com.brahma.arogya.operations.dto.IntegratedOperationsAdmitRequest;
import com.brahma.arogya.operations.dto.IntegratedOperationsInvoiceRequest;
import com.brahma.arogya.operations.dto.IntegratedOperationsLabRequest;
import com.brahma.arogya.operations.dto.IntegratedOperationsLabStatusRequest;
import com.brahma.arogya.operations.dto.IntegratedOperationsPaymentRequest;
import com.brahma.arogya.operations.dto.IntegratedOperationsStockRequest;
import com.brahma.arogya.operations.service.IntegratedOperationsService;
import java.security.Principal;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/operations")
public class IntegratedOperationsController {

  private final IntegratedOperationsService service;

  public IntegratedOperationsController(IntegratedOperationsService service) {
    this.service = service;
  }

  @GetMapping("/lab/items")
  public List<Map<String, Object>> labItems() {
    return service.labItems();
  }

  @PostMapping("/lab/orders")
  public Map<String, Object> labOrder(
      @RequestBody @jakarta.validation.Valid() IntegratedOperationsLabRequest req, Principal p) {
    return service.labOrder(req, p);
  }

  @PostMapping("/lab/items/{id}/transition")
  public Map<String, Object> labTransition(
      @PathVariable long id,
      @RequestBody @jakarta.validation.Valid() IntegratedOperationsLabStatusRequest r,
      Principal p) {
    return service.labTransition(id, r, p);
  }

  @PostMapping("/billing/invoices")
  public Map<String, Object> invoice(
      @RequestBody @jakarta.validation.Valid() IntegratedOperationsInvoiceRequest r, Principal p) {
    return service.invoice(r, p);
  }

  @PostMapping("/billing/invoices/{id}/payments")
  public Map<String, Object> payment(
      @PathVariable long id,
      @RequestBody @jakarta.validation.Valid() IntegratedOperationsPaymentRequest r,
      Principal p) {
    return service.payment(id, r, p);
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  @PostMapping("/pharmacy/inventory/{id}/dispense")
  public Map<String, Object> dispense(
      @PathVariable long id,
      @RequestBody @jakarta.validation.Valid() IntegratedOperationsStockRequest r,
      Principal p) {
    return service.dispense(id, r, p);
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  @PostMapping("/pharmacy/inventory/{id}/receive")
  public Map<String, Object> receive(
      @PathVariable long id,
      @RequestBody @jakarta.validation.Valid() IntegratedOperationsStockRequest r,
      Principal p) {
    return service.receive(id, r, p);
  }

  @PostMapping("/ipd/admit")
  public Map<String, Object> admit(
      @RequestBody @jakarta.validation.Valid() IntegratedOperationsAdmitRequest r, Principal p) {
    return service.admit(r, p);
  }

  @PostMapping("/ipd/admissions/{id}/discharge")
  public Map<String, Object> discharge(@PathVariable long id, Principal p) {
    return service.discharge(id, p);
  }

  @GetMapping("/ipd/beds")
  public List<Map<String, Object>> beds() {
    return service.beds();
  }
}
