package com.brahma.arogya.billing.controller;

import com.brahma.arogya.billing.service.VisitBillingService;
import java.math.*;
import java.security.Principal;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workflow/encounters")
public class VisitBillingController {

  private final VisitBillingService service;

  public VisitBillingController(VisitBillingService service) {
    this.service = service;
  }

  @PostMapping("/{id}/billing")
  public Map<String, String> billing(@PathVariable long id, Principal p) {
    return service.billing(id, p);
  }
}
