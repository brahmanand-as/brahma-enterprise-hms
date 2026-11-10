package com.brahma.arogya.security.controller;

import com.brahma.arogya.security.dto.SecurityAdministrationAccountChangeRequest;
import com.brahma.arogya.security.dto.SecurityAdministrationRoleFormRequest;
import com.brahma.arogya.security.dto.SecurityAdministrationSettingRequest;
import com.brahma.arogya.security.service.SecurityAdministrationService;
import java.security.Principal;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/security")
public class SecurityAdministration {

  private final SecurityAdministrationService service;

  public SecurityAdministration(SecurityAdministrationService service) {
    this.service = service;
  }

  @GetMapping("/permissions")
  public List<Map<String, Object>> permissions() {
    return service.permissions();
  }

  @GetMapping("/roles")
  public List<Map<String, Object>> roles() {
    return service.roles();
  }

  @PostMapping("/roles")
  public Map<String, String> role(
      @RequestBody @jakarta.validation.Valid() SecurityAdministrationRoleFormRequest f) {
    return service.role(f);
  }

  @PutMapping("/roles/{name}")
  public Map<String, String> update(
      @PathVariable String name,
      @RequestBody @jakarta.validation.Valid() SecurityAdministrationRoleFormRequest f) {
    return service.update(name, f);
  }

  @GetMapping("/accounts")
  public List<Map<String, Object>> accounts() {
    return service.accounts();
  }

  @PatchMapping("/accounts/{id}")
  public Map<String, String> change(
      @PathVariable long id,
      @RequestBody @jakarta.validation.Valid() SecurityAdministrationAccountChangeRequest f,
      Principal p) {
    return service.change(id, f, p);
  }

  @GetMapping("/audit")
  public List<Map<String, Object>> audit(@RequestParam(defaultValue = "200") int limit) {
    return service.audit(limit);
  }

  @GetMapping("/settings")
  public List<Map<String, Object>> settings() {
    return service.settings();
  }

  @PutMapping("/settings/{key}")
  public Map<String, String> setting(
      @PathVariable String key,
      @RequestBody @jakarta.validation.Valid() SecurityAdministrationSettingRequest f) {
    return service.setting(key, f);
  }
}
