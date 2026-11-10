package com.brahma.arogya.clinical.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.brahma.arogya.appointment.entity.Appointment;
import com.brahma.arogya.appointment.repository.AppointmentRepository;
import com.brahma.arogya.clinical.dto.ClinicalWorkflowWalkInRequest;
import com.brahma.arogya.clinical.entity.Encounter;
import com.brahma.arogya.clinical.entity.Vital;
import com.brahma.arogya.clinical.repository.ConsultationRepository;
import com.brahma.arogya.clinical.repository.EncounterRepository;
import com.brahma.arogya.clinical.repository.VitalRepository;
import com.brahma.arogya.patient.repository.PatientRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ClinicalWorkflowServiceTest {
  @Mock AppointmentRepository appointments;
  @Mock EncounterRepository encounters;
  @Mock PatientRepository patients;
  @Mock VitalRepository vitals;
  @Mock ConsultationRepository consultations;
  ClinicalWorkflowService controller;

  @BeforeEach
  void setup() {
    controller =
        new ClinicalWorkflowService(
            appointments, encounters, patients, vitals, consultations, null, null);
  }

  @Test
  void cancelledAppointmentCannotCheckIn() {
    Appointment a = new Appointment();
    a.setStatus("CANCELLED");
    when(appointments.findById(7L)).thenReturn(Optional.of(a));
    assertThrows(ResponseStatusException.class, () -> controller.checkIn(7L));
    verifyNoInteractions(encounters);
  }

  @Test
  void checkInIsIdempotentForExistingEncounter() {
    Appointment a = new Appointment();
    a.setPatientId(3L);
    a.setStatus("BOOKED");
    Encounter existing = new Encounter();
    existing.setId(11L);
    existing.setStatus("CHECKED_IN");
    when(appointments.findById(7L)).thenReturn(Optional.of(a));
    when(patients.existsById(3L)).thenReturn(true);
    when(encounters.findByAppointmentId(7L)).thenReturn(Optional.of(existing));
    assertSame(existing, controller.checkIn(7L));
    verify(encounters, never()).save(any());
  }

  @Test
  void walkInRequiresExistingPatient() {
    when(patients.existsById(99L)).thenReturn(false);
    assertThrows(
        ResponseStatusException.class,
        () -> controller.walkIn(new ClinicalWorkflowWalkInRequest(99L, "Doctor", "Pain")));
  }

  @Test
  void consultationCannotStartForCompletedEncounter() {
    Encounter e = new Encounter();
    e.setStatus("COMPLETED");
    when(encounters.findById(5L)).thenReturn(Optional.of(e));
    assertThrows(ResponseStatusException.class, () -> controller.startConsultation(5L));
    verify(encounters, never()).save(any());
  }

  @Test
  void recordingVitalsMovesEncounterToWaiting() {
    Encounter e = new Encounter();
    e.setStatus("CHECKED_IN");
    when(encounters.findById(5L)).thenReturn(Optional.of(e));
    when(vitals.save(any(Vital.class))).thenAnswer(i -> i.getArgument(0));
    when(encounters.save(any(Encounter.class))).thenAnswer(i -> i.getArgument(0));
    Vital saved = controller.recordVitals(5L, new Vital());
    assertEquals(5L, saved.getEncounterId());
    assertNotNull(saved.getRecordedAt());
    assertEquals("WAITING", e.getStatus());
  }
}
