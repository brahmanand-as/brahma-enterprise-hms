package com.brahma.arogya.security.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.brahma.arogya.clinical.dto.EncounterResponse;
import com.brahma.arogya.clinical.dto.VitalResponse;
import com.brahma.arogya.patient.dto.PatientResponse;
import com.brahma.arogya.security.repository.CareScopeJdbcRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class CareScopeDtoTest {
  @Test
  void mappedPatientAndEncounterResponsesStillEnforceCareAssignment() {
    var db = mock(CareScopeJdbcRepository.class);
    var care = new CareScope(db, new AccessPolicy());
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(
                "nurse", "", List.of(new SimpleGrantedAuthority("ROLE_NURSE"))));
    try {
      when(db.countCareTeamsForCan(3L, "nurse", 3L, "nurse", 3L, "nurse")).thenReturn(0);
      var patient = new PatientResponse();
      patient.setId(3L);
      assertFalse(care.visible(patient));
      var encounter = new EncounterResponse();
      encounter.setPatientId(3L);
      assertFalse(care.visible(encounter));
      when(db.countCareTeamsForCan(3L, "nurse", 3L, "nurse", 3L, "nurse")).thenReturn(1);
      assertTrue(care.visible(patient));
      assertTrue(care.visible(encounter));
    } finally {
      SecurityContextHolder.clearContext();
    }
  }

  @Test
  void responseAdviceFiltersMappedListsBeforeSerialization() {
    var care = mock(CareScope.class);
    when(care.restricted()).thenReturn(true);
    var visible = new PatientResponse();
    visible.setId(1L);
    var hidden = new PatientResponse();
    hidden.setId(2L);
    when(care.visible(visible)).thenReturn(true);
    when(care.visible(hidden)).thenReturn(false);
    var result =
        new ScopedResponseAdvice(care)
            .beforeBodyWrite(List.of(visible, hidden), null, null, null, null, null);
    assertEquals(List.of(visible), result);
  }

  @Test
  void mappedVitalResponseResolvesEncounterOwnershipAndFailsClosedWhenMissing() {
    var db = mock(CareScopeJdbcRepository.class);
    var care = new CareScope(db, new AccessPolicy());
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(
                "nurse", "", List.of(new SimpleGrantedAuthority("ROLE_NURSE"))));
    try {
      var vital = new VitalResponse();
      vital.setEncounterId(9L);
      when(db.readEncountersForVisible(9L)).thenReturn(3L);
      when(db.countCareTeamsForCan(3L, "nurse", 3L, "nurse", 3L, "nurse")).thenReturn(0);
      assertFalse(care.visible(vital));
      when(db.countCareTeamsForCan(3L, "nurse", 3L, "nurse", 3L, "nurse")).thenReturn(1);
      assertTrue(care.visible(vital));
      when(db.readEncountersForVisible(9L))
          .thenThrow(new IllegalStateException("missing encounter"));
      assertFalse(care.visible(vital));
    } finally {
      SecurityContextHolder.clearContext();
    }
  }
}
