package com.brahma.arogya.security.controller;

import com.brahma.arogya.security.dto.AuthLoginRequest;
import com.brahma.arogya.security.service.AuthService;
import java.util.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService service;

  public AuthController(AuthService service) {
    this.service = service;
  }

  @PostMapping("/login")
  public Map<String, Object> login(@RequestBody @jakarta.validation.Valid() AuthLoginRequest r) {
    return service.login(r);
  }

  @GetMapping("/me")
  public Map<String, Object> me(Authentication auth) {
    return service.me(auth);
  }
}
