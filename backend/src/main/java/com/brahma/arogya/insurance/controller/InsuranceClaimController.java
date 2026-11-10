package com.brahma.arogya.insurance.controller;

import com.brahma.arogya.insurance.dto.InsuranceClaimRequest;
import com.brahma.arogya.insurance.dto.InsuranceClaimResponse;
import com.brahma.arogya.insurance.mapper.InsuranceClaimMapper;
import com.brahma.arogya.insurance.service.InsuranceClaimService;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/insurance")
public class InsuranceClaimController {

  private final InsuranceClaimService service;

  public InsuranceClaimController(InsuranceClaimService service) {
    this.service = service;
  }

  @GetMapping
  public List<InsuranceClaimResponse> all() {
    return service.all().stream().map(InsuranceClaimMapper::toResponse).toList();
  }

  @GetMapping("/{id}")
  public InsuranceClaimResponse one(@PathVariable Long id) {
    return InsuranceClaimMapper.toResponse(service.one(id));
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  @PostMapping
  public InsuranceClaimResponse create(
      @RequestBody @jakarta.validation.Valid() InsuranceClaimRequest x) {
    return InsuranceClaimMapper.toResponse(service.create(InsuranceClaimMapper.toEntity(x)));
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  @PutMapping("/{id}")
  public InsuranceClaimResponse update(
      @PathVariable Long id, @RequestBody @jakarta.validation.Valid() InsuranceClaimRequest x) {
    return InsuranceClaimMapper.toResponse(service.update(id, InsuranceClaimMapper.toEntity(x)));
  }

  @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }
}
