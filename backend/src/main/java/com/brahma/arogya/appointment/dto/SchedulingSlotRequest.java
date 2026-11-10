package com.brahma.arogya.appointment.dto;

import java.time.*;
import java.util.*;

public record SchedulingSlotRequest(
    String doctorName,
    LocalDateTime startsAt,
    LocalDateTime endsAt,
    @jakarta.validation.constraints.Positive() Long doctorEmployeeId) {

  public SchedulingSlotRequest(String name, LocalDateTime start, LocalDateTime end) {
    this(name, start, end, null);
  }
}
