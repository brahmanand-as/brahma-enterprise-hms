package com.brahma.arogya.portal.controller;

import com.brahma.arogya.portal.service.PortalDataService;
import java.util.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/portal")
public class PortalDataController {

  private final PortalDataService service;

  public PortalDataController(PortalDataService service) {
    this.service = service;
  }

  @GetMapping("/options")
  public Map<String, Object> options(Authentication a) {
    return service.options(a);
  }
}
