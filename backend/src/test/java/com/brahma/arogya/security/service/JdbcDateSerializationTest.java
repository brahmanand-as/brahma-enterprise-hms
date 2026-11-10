package com.brahma.arogya.security.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.Test;

class JdbcDateSerializationTest {
  @Test
  void calendarDatesDoNotShiftWithJsonTimezone() {
    var map =
        Map.of(
            "shift_date",
            java.sql.Date.valueOf("2026-11-27"),
            "starts_at",
            java.sql.Time.valueOf("09:00:00"));
    var normalized = (Map<?, ?>) ScopedResponseAdvice.normalize(map);
    assertEquals("2026-11-27", normalized.get("shift_date"));
    assertEquals("09:00", normalized.get("starts_at"));
  }
}
