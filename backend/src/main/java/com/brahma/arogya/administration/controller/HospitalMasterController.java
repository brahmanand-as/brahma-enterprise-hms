package com.brahma.arogya.administration.controller;

import com.brahma.arogya.administration.dto.HospitalMasterDepartmentRequest;
import com.brahma.arogya.administration.service.HospitalMasterService;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/masters")
public class HospitalMasterController {

  private final HospitalMasterService service;

  public HospitalMasterController(HospitalMasterService service) {
    this.service = service;
  }

  @GetMapping("/departments")
  public List<Map<String, Object>> departments() {
    return service.departments();
  }

  @PostMapping("/departments")
  public Map<String, String> create(
      @RequestBody @jakarta.validation.Valid() HospitalMasterDepartmentRequest f) {
    return service.create(f);
  }
}
