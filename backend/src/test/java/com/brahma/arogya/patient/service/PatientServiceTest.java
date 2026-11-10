package com.brahma.arogya.patient.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.brahma.arogya.patient.entity.Patient;
import com.brahma.arogya.patient.repository.PatientJdbcRepository;
import com.brahma.arogya.patient.repository.PatientRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class PatientServiceTest {
  @Test
  void assignsSequentialMrnAndIgnoresClientMrn() {
    var repo = mock(PatientRepository.class);
    var db = mock(PatientJdbcRepository.class);
    when(db.readPatientNumberSequenceForCreate()).thenReturn(42L);
    when(repo.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
    var p = new Patient();
    p.setFirstName("Synthetic");
    p.setUhid("CLIENT");
    var saved = new PatientService(repo, db, null, null).create(p);
    assertEquals("MRN-00000042", saved.getUhid());
    verify(db).updatePatientNumberSequenceForCreate();
  }

  @Test
  void updatePreservesMrn() {
    var repo = mock(PatientRepository.class);
    var old = new Patient();
    old.setUhid("MRN-00000001");
    when(repo.findById(1L)).thenReturn(Optional.of(old));
    when(repo.save(any())).thenAnswer(i -> i.getArgument(0));
    var changed = new Patient();
    changed.setFirstName("Updated");
    changed.setUhid("OTHER");
    assertEquals(
        "MRN-00000001",
        new PatientService(repo, mock(PatientJdbcRepository.class), null, null)
            .update(1L, changed)
            .getUhid());
  }

  @Test
  void rejectsClientPrimaryKey() {
    var p = new Patient();
    p.setId(7L);
    p.setFirstName("Synthetic");
    assertThrows(
        org.springframework.web.server.ResponseStatusException.class,
        () ->
            new PatientService(
                    mock(PatientRepository.class), mock(PatientJdbcRepository.class), null, null)
                .create(p));
  }
}
