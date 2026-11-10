package com.brahma.arogya.security.controller;

import com.brahma.arogya.security.dto.CareAssignmentRequest;
import com.brahma.arogya.security.service.CareAssignmentService;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/care")
public class CareAssignmentController {

  private final CareAssignmentService service;

  public CareAssignmentController(CareAssignmentService service) {
    this.service = service;
  }

  @GetMapping("/teams")
  public List<Map<String, Object>> teams() {
    return service.teams();
  }

  @PostMapping("/teams")
  public void assign(@RequestBody @jakarta.validation.Valid() CareAssignmentRequest f) {
    service.assign(f);
  }

  @DeleteMapping("/teams")
  public void remove(@RequestParam long patientId, @RequestParam String username) {
    service.remove(patientId, username);
  }
}
