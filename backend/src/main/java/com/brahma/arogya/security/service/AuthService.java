package com.brahma.arogya.security.service;

import com.brahma.arogya.security.dto.AuthLoginRequest;
import com.brahma.arogya.security.repository.AuthJdbcRepository;
import java.util.*;
import org.springframework.security.core.Authentication;

@org.springframework.stereotype.Service()
public class AuthService {

  private final LoginThrottle throttle;

  private final AuthJdbcRepository db;

  private final JwtService jwt;

  private final org.springframework.security.core.userdetails.UserDetailsService users;

  private final org.springframework.security.crypto.password.PasswordEncoder encoder;

  public AuthService(
      AuthJdbcRepository db,
      JwtService jwt,
      org.springframework.security.core.userdetails.UserDetailsService users,
      org.springframework.security.crypto.password.PasswordEncoder encoder,
      LoginThrottle throttle) {
    this.db = db;
    this.jwt = jwt;
    this.users = users;
    this.encoder = encoder;
    this.throttle = throttle;
  }

  public Map<String, Object> login(AuthLoginRequest r) {
    throttle.check(String.valueOf(r.username()));
    try {
      var user = users.loadUserByUsername(r.username());
      if (!user.isEnabled() || !encoder.matches(r.password(), user.getPassword()))
        throw new IllegalArgumentException();
      throttle.clear(String.valueOf(r.username()));
      return Map.of(
          "token",
          jwt.issue(user.getUsername(), db.readStaffAccountsForLogin(user.getUsername())),
          "expiresIn",
          3600);
    } catch (Exception e) {
      throttle.failed(String.valueOf(r.username()));
      throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.UNAUTHORIZED, "Invalid credentials");
    }
  }

  public Map<String, Object> me(Authentication auth) {
    List<Map<String, Object>> staff = db.findStaffAccountsForMe(auth.getName());
    Map<String, Object> result = new HashMap<>();
    result.put("username", auth.getName());
    result.put(
        "permissions",
        auth.getAuthorities().stream()
            .map(Object::toString)
            .filter(s -> s.startsWith("PERM_"))
            .map(s -> s.substring(5))
            .toList());
    result.put("roles", auth.getAuthorities().stream().map(Object::toString).toList());
    if (!staff.isEmpty()) {
      result.put("role", staff.get(0).get("role"));
      result.put("employeeId", staff.get(0).get("employee_id"));
    }
    return result;
  }
}
