package com.brahma.arogya.insurance.controller;

import com.brahma.arogya.insurance.dto.InsuranceIntakeRequest;
import com.brahma.arogya.insurance.service.InsuranceIntakeService;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/enterprise/claims")
public class InsuranceIntakeController {

  private final InsuranceIntakeService service;

  public InsuranceIntakeController(InsuranceIntakeService service) {
    this.service = service;
  }

  @PostMapping
  public Map<String, Object> create(
      @RequestBody @jakarta.validation.Valid() InsuranceIntakeRequest r) {
    return service.create(r);
  }
}
