package com.brahma.arogya.patient.controller;

import com.brahma.arogya.patient.service.PatientTimelineService;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clinical")
public class PatientTimelineController {

  private final PatientTimelineService service;

  public PatientTimelineController(PatientTimelineService service) {
    this.service = service;
  }

  @GetMapping("/patients/{id}")
  public Map<String, Object> timeline(@PathVariable long id) {
    return service.timeline(id);
  }
}
