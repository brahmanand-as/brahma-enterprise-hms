package com.brahma.arogya.patient.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.brahma.arogya.patient.repository.PatientJdbcRepository;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class PatientJdbcRepositoryTest {
  @Test
  void mrnAllocationRetainsTheLockingQuery() {
    var jdbc = mock(JdbcTemplate.class);
    when(jdbc.queryForObject(
            "SELECT next_value FROM patient_number_sequence WHERE id=1 FOR UPDATE", Long.class))
        .thenReturn(42L);
    var repository = new PatientJdbcRepository(jdbc);
    assertEquals(42L, repository.readPatientNumberSequenceForCreate());
    verify(jdbc)
        .queryForObject(
            "SELECT next_value FROM patient_number_sequence WHERE id=1 FOR UPDATE", Long.class);
  }
}
