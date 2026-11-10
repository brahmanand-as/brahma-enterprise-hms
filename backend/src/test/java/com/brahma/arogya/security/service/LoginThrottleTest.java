package com.brahma.arogya.security.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class LoginThrottleTest {
  @Test
  void fiveFailuresAreThrottledAndOtherAccountsRemainUsable() {
    var limiter = new LoginThrottle();
    for (int i = 0; i < 5; i++) limiter.failed("synthetic-user");
    var e = assertThrows(ResponseStatusException.class, () -> limiter.check("synthetic-user"));
    assertEquals(429, e.getStatusCode().value());
    assertDoesNotThrow(() -> limiter.check("other-user"));
  }

  @Test
  void successfulSignInClearsFailures() {
    var limiter = new LoginThrottle();
    for (int i = 0; i < 5; i++) limiter.failed("synthetic-user");
    limiter.clear("synthetic-user");
    assertDoesNotThrow(() -> limiter.check("synthetic-user"));
  }
}
