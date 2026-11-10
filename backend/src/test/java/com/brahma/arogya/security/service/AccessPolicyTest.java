package com.brahma.arogya.security.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class AccessPolicyTest {
  private UsernamePasswordAuthenticationToken user(String role, String... permissions) {
    List<SimpleGrantedAuthority> a = new ArrayList<>();
    a.add(new SimpleGrantedAuthority("ROLE_" + role));
    for (String p : permissions) a.add(new SimpleGrantedAuthority("PERM_" + p));
    return new UsernamePasswordAuthenticationToken("test", null, a);
  }

  @Test
  void customReadRoleCannotWritePatientsOrReadFinance() {
    var policy = new AccessPolicy();
    var u = user("RECORDS_VIEWER", "patients.read");
    assertTrue(policy.allowed(u, new MockHttpServletRequest("GET", "/api/patients")));
    assertFalse(policy.allowed(u, new MockHttpServletRequest("POST", "/api/patients")));
    assertFalse(policy.allowed(u, new MockHttpServletRequest("GET", "/api/finance/invoices")));
  }

  @Test
  void patientCannotReuseAStaffPermission() {
    var policy = new AccessPolicy();
    var patient = user("PATIENT", "security.manage", "patients.read");
    assertFalse(policy.allowed(patient, new MockHttpServletRequest("GET", "/api/security/roles")));
    assertFalse(policy.allowed(patient, new MockHttpServletRequest("GET", "/api/patients")));
    assertTrue(
        policy.allowed(patient, new MockHttpServletRequest("GET", "/api/patient/appointments")));
  }

  @Test
  void customPathologistCanReachVerificationButCannotOrder() {
    var p = new AccessPolicy();
    var u = user("QUALITY_VERIFIER", "lab.verify");
    assertTrue(
        p.allowed(u, new MockHttpServletRequest("POST", "/api/operations/lab/items/7/transition")));
    assertFalse(p.allowed(u, new MockHttpServletRequest("POST", "/api/operations/lab/orders")));
  }

  @Test
  void unknownEndpointsDefaultToDeny() {
    assertFalse(
        new AccessPolicy()
            .allowed(
                user("RECORDS_VIEWER", "patients.read"),
                new MockHttpServletRequest("GET", "/api/future-sensitive-module")));
  }
}
