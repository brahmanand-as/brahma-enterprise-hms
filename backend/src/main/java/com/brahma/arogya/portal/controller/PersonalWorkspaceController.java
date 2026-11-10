package com.brahma.arogya.portal.controller;

import com.brahma.arogya.portal.dto.PersonalWorkspacePasswordChangeRequest;
import com.brahma.arogya.portal.service.PersonalWorkspaceService;
import java.security.Principal;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class PersonalWorkspaceController {

  private final PersonalWorkspaceService service;

  public PersonalWorkspaceController(PersonalWorkspaceService service) {
    this.service = service;
  }

  @GetMapping("/notifications")
  public List<Map<String, Object>> notifications(Principal p) {
    return service.notifications(p);
  }

  @PostMapping("/notifications/{id}/read")
  public void read(@PathVariable long id, Principal p) {
    service.read(id, p);
  }

  @PostMapping("/auth/password")
  public void password(
      @RequestBody @jakarta.validation.Valid() PersonalWorkspacePasswordChangeRequest f,
      Principal p) {
    service.password(f, p);
  }

  @PostMapping("/auth/logout")
  public void logout(Principal p) {
    service.logout(p);
  }
}
