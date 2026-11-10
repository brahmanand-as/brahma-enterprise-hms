package com.brahma.arogya.hr.controller;

import com.brahma.arogya.hr.dto.DoctorProfileRequest;
import com.brahma.arogya.hr.service.DoctorProfileService;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/doctor-profiles")
public class DoctorProfileController {

  private final DoctorProfileService service;

  public DoctorProfileController(DoctorProfileService service) {
    this.service = service;
  }

  @GetMapping
  public List<Map<String, Object>> all() {
    return service.all();
  }

  @GetMapping("/mine")
  public List<Map<String, Object>> mine(org.springframework.security.core.Authentication auth) {
    return service.mine(auth);
  }

  @PostMapping
  public Map<String, Object> create(
      @RequestBody @jakarta.validation.Valid() DoctorProfileRequest x) {
    return service.create(x);
  }
}
