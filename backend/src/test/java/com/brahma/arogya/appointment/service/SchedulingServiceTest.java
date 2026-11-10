package com.brahma.arogya.appointment.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.brahma.arogya.appointment.dto.SchedulingSlotRequest;
import com.brahma.arogya.appointment.repository.SchedulingJdbcRepository;
import java.time.*;
import org.junit.jupiter.api.Test;

class SchedulingServiceTest {
  @Test
  void rejectsPastSlot() {
    var db = mock(SchedulingJdbcRepository.class);
    var now = LocalDateTime.now();
    assertThrows(
        org.springframework.web.server.ResponseStatusException.class,
        () ->
            new SchedulingService(db, null, null)
                .slot(new SchedulingSlotRequest("Doctor", now.minusDays(1), now)));
    verifyNoInteractions(db);
  }

  @Test
  void rejectsInvertedSlot() {
    var db = mock(SchedulingJdbcRepository.class);
    var now = LocalDateTime.now().plusDays(1);
    assertThrows(
        org.springframework.web.server.ResponseStatusException.class,
        () ->
            new SchedulingService(db, null, null)
                .slot(new SchedulingSlotRequest("Doctor", now, now.minusMinutes(1))));
    verifyNoInteractions(db);
  }
}
