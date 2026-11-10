package com.brahma.arogya.laboratory.controller;

import com.brahma.arogya.laboratory.dto.LabDispatchDispatchRequest;
import com.brahma.arogya.laboratory.service.LabDispatchService;
import java.security.Principal;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/operations/lab/dispatches")
public class LabDispatchController {

  private final LabDispatchService service;

  public LabDispatchController(LabDispatchService service) {
    this.service = service;
  }

  @GetMapping
  public List<Map<String, Object>> all() {
    return service.all();
  }

  @PostMapping
  public Map<String, String> dispatch(
      @RequestBody @jakarta.validation.Valid() LabDispatchDispatchRequest r, Principal p) {
    return service.dispatch(r, p);
  }

  @PostMapping("/{id}/receive")
  public Map<String, String> receive(@PathVariable long id) {
    return service.receive(id);
  }
}
