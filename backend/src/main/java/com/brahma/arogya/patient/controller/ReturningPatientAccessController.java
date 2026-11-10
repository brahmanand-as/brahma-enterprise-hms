package com.brahma.arogya.patient.controller;

import com.brahma.arogya.patient.dto.ReturningPatientAccessRequest;
import com.brahma.arogya.patient.service.ReturningPatientAccessService;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scheduling/patient-access")
public class ReturningPatientAccessController {

  private final ReturningPatientAccessService service;

  public ReturningPatientAccessController(ReturningPatientAccessService service) {
    this.service = service;
  }

  @PostMapping
  public Map<String, String> link(
      @RequestBody @jakarta.validation.Valid() ReturningPatientAccessRequest f) {
    return service.link(f);
  }
}
