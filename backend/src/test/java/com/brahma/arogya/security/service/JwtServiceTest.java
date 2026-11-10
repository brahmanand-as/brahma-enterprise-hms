package com.brahma.arogya.security.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class JwtServiceTest {
  @Test
  void roundTrip() {
    var service = new JwtService("synthetic-secret-for-unit-tests-1234567890");
    assertEquals("demo.doctor", service.subject(service.issue("demo.doctor")));
  }

  @Test
  void rejectsTokenSignedWithAnotherKey() {
    var a = new JwtService("synthetic-secret-for-unit-tests-1234567890");
    var b = new JwtService("different-secret-for-unit-tests-0987654321");
    assertThrows(Exception.class, () -> b.subject(a.issue("admin")));
  }

  @Test
  void rejectsShortSecrets() {
    assertThrows(IllegalStateException.class, () -> new JwtService("short"));
  }
}
