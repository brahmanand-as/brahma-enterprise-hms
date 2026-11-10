package com.brahma.arogya.analytics.controller;

import com.brahma.arogya.analytics.dto.AnalyticsReportQualityRequest;
import com.brahma.arogya.analytics.service.AnalyticsReportService;
import java.io.*;
import java.math.*;
import java.security.Principal;
import java.time.*;
import java.util.*;
import java.util.zip.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class AnalyticsReportController {

  private final AnalyticsReportService service;

  public AnalyticsReportController(AnalyticsReportService service) {
    this.service = service;
  }

  @GetMapping("/overview")
  public Map<String, Object> overview(
      @RequestParam String month, @RequestParam(defaultValue = "") String department) {
    return service.overview(month, department);
  }

  @PostMapping("/quality")
  public void quality(
      @RequestBody @jakarta.validation.Valid() AnalyticsReportQualityRequest f, Principal p) {
    service.quality(f, p);
  }

  @GetMapping("/export.xlsx")
  public ResponseEntity<byte[]> xlsx(
      @RequestParam String month, @RequestParam(defaultValue = "") String department)
      throws Exception {
    return service.xlsx(month, department);
  }

  @GetMapping("/export.pdf")
  public ResponseEntity<byte[]> pdf(
      @RequestParam String month, @RequestParam(defaultValue = "") String department)
      throws Exception {
    return service.pdf(month, department);
  }
}
