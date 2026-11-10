package com.brahma.arogya.billing.controller;

import com.brahma.arogya.billing.dto.FinancialAdjustmentRequest;
import com.brahma.arogya.billing.dto.FinancialCreditReconcileRequest;
import com.brahma.arogya.billing.dto.FinancialRefundRequest;
import com.brahma.arogya.billing.dto.FinancialSplitRequest;
import com.brahma.arogya.billing.service.FinancialService;
import java.math.*;
import java.security.Principal;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/finance")
public class FinancialController {

  private final FinancialService service;

  public FinancialController(FinancialService service) {
    this.service = service;
  }

  @GetMapping("/invoices")
  public List<Map<String, Object>> invoices() {
    return service.invoices();
  }

  @GetMapping("/invoices/{id}")
  public Map<String, Object> invoice(@PathVariable long id) {
    return service.invoice(id);
  }

  @PostMapping("/invoices/{id}/payments")
  public Map<String, Object> pay(
      @PathVariable long id,
      @RequestBody @jakarta.validation.Valid() FinancialSplitRequest r,
      Principal p) {
    return service.pay(id, r, p);
  }

  @PostMapping("/invoices/{id}/adjustments")
  public Map<String, Object> adjust(
      @PathVariable long id,
      @RequestBody @jakarta.validation.Valid() FinancialAdjustmentRequest f,
      Principal p) {
    return service.adjust(id, f, p);
  }

  @PostMapping("/invoices/{id}/refunds")
  public Map<String, Object> refund(
      @PathVariable long id,
      @RequestBody @jakarta.validation.Valid() FinancialRefundRequest f,
      Principal p) {
    return service.refund(id, f, p);
  }

  @GetMapping("/return-credits")
  public List<Map<String, Object>> credits() {
    return service.credits();
  }

  @PostMapping("/return-credits/{id}/reconcile")
  public Map<String, String> reconcileCredit(
      @PathVariable long id,
      @RequestBody @jakarta.validation.Valid() FinancialCreditReconcileRequest f,
      Principal p) {
    return service.reconcileCredit(id, f, p);
  }
}
