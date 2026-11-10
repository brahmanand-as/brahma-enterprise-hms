package com.brahma.arogya.analytics.controller;

import com.brahma.arogya.analytics.service.DashboardService;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

  private final DashboardService service;

  public DashboardController(DashboardService service) {
    this.service = service;
  }

  @GetMapping
  public Map<String, Long> stats() {
    return service.stats();
  }
}
