package com.brahma.arogya.pharmacy.controller;

import com.brahma.arogya.pharmacy.dto.PharmacyConsumableReturnRequest;
import com.brahma.arogya.pharmacy.dto.PharmacyDispenseRequest;
import com.brahma.arogya.pharmacy.dto.PharmacyPurchaseRequest;
import com.brahma.arogya.pharmacy.dto.PharmacyReceiptRequest;
import com.brahma.arogya.pharmacy.dto.PharmacyStockActionRequest;
import com.brahma.arogya.pharmacy.dto.PharmacySupplierRequest;
import com.brahma.arogya.pharmacy.service.PharmacyService;
import java.math.*;
import java.security.Principal;
import java.time.*;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pharmacy")
public class PharmacyController {

  private final PharmacyService service;

  public PharmacyController(PharmacyService service) {
    this.service = service;
  }

  @GetMapping("/batches")
  public List<Map<String, Object>> batches() {
    return service.batches();
  }

  @GetMapping("/suppliers")
  public List<Map<String, Object>> suppliers() {
    return service.suppliers();
  }

  @PostMapping("/suppliers")
  public Map<String, String> supplier(
      @RequestBody @jakarta.validation.Valid() PharmacySupplierRequest s) {
    return service.supplier(s);
  }

  @GetMapping("/purchases")
  public List<Map<String, Object>> purchases() {
    return service.purchases();
  }

  @PostMapping("/purchases")
  public Map<String, Object> purchase(
      @RequestBody @jakarta.validation.Valid() PharmacyPurchaseRequest f, Principal p) {
    return service.purchase(f, p);
  }

  @PostMapping("/receipts")
  public Map<String, Object> receipt(
      @RequestBody @jakarta.validation.Valid() PharmacyReceiptRequest f, Principal p) {
    return service.receipt(f, p);
  }

  @GetMapping("/dispenses")
  public List<Map<String, Object>> dispenses() {
    return service.dispenses();
  }

  @PostMapping("/dispenses")
  public Map<String, Object> dispense(
      @RequestBody @jakarta.validation.Valid() PharmacyDispenseRequest f, Principal p) {
    return service.dispense(f, p);
  }

  @PostMapping("/dispenses/{id}/return")
  public Map<String, Object> returns(
      @PathVariable long id,
      @RequestBody @jakarta.validation.Valid() PharmacyStockActionRequest f,
      Principal p) {
    return service.returns(id, f, p);
  }

  @PostMapping("/batches/{id}/action")
  public Map<String, String> action(
      @PathVariable long id,
      @RequestParam String action,
      @RequestBody @jakarta.validation.Valid() PharmacyStockActionRequest f,
      Principal p) {
    return service.action(id, action, f, p);
  }

  @GetMapping("/ward-stock")
  public List<Map<String, Object>> wardStock() {
    return service.wardStock();
  }

  @PostMapping("/ward-stock/{id}/consume")
  public Map<String, String> consume(
      @PathVariable long id,
      @RequestBody @jakarta.validation.Valid() PharmacyStockActionRequest f,
      Principal p) {
    return service.consume(id, f, p);
  }

  @PostMapping("/ward-stock/{id}/action")
  public Map<String, String> wardAction(
      @PathVariable long id,
      @RequestParam String action,
      @RequestBody @jakarta.validation.Valid() PharmacyStockActionRequest f,
      Principal p) {
    return service.wardAction(id, action, f, p);
  }

  @PostMapping("/consumable-returns")
  public Map<String, String> consumableReturn(
      @RequestBody @jakarta.validation.Valid() PharmacyConsumableReturnRequest f, Principal p) {
    return service.consumableReturn(f, p);
  }

  @GetMapping("/patient-consumables")
  public List<Map<String, Object>> consumables() {
    return service.consumables();
  }

  @GetMapping("/movements")
  public List<Map<String, Object>> movements() {
    return service.movements();
  }
}
