package com.brahma.arogya.billing.controller;

import com.brahma.arogya.billing.dto.InvoiceRequest;
import com.brahma.arogya.billing.dto.InvoiceResponse;
import com.brahma.arogya.billing.mapper.InvoiceMapper;
import com.brahma.arogya.billing.service.InvoiceService;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

  private final InvoiceService service;

  public InvoiceController(InvoiceService service) {
    this.service = service;
  }

  @GetMapping
  public List<InvoiceResponse> all() {
    return service.all().stream().map(InvoiceMapper::toResponse).toList();
  }

  @GetMapping("/{id}")
  public InvoiceResponse one(@PathVariable Long id) {
    return InvoiceMapper.toResponse(service.one(id));
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  @PostMapping
  public InvoiceResponse create(@RequestBody @jakarta.validation.Valid() InvoiceRequest x) {
    return InvoiceMapper.toResponse(service.create(InvoiceMapper.toEntity(x)));
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  @PutMapping("/{id}")
  public InvoiceResponse update(
      @PathVariable Long id, @RequestBody @jakarta.validation.Valid() InvoiceRequest x) {
    return InvoiceMapper.toResponse(service.update(id, InvoiceMapper.toEntity(x)));
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }
}
