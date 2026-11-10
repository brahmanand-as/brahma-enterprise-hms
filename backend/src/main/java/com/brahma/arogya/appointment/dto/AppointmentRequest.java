package com.brahma.arogya.appointment.dto;

import java.math.*;
import java.time.*;

@lombok.Getter()
@lombok.Setter()
@lombok.NoArgsConstructor()
public class AppointmentRequest {

  @jakarta.validation.constraints.Positive() private Long id;

  @jakarta.validation.constraints.Positive() private Long doctorEmployeeId;

  @jakarta.validation.constraints.Positive() private Long patientId;

  @jakarta.validation.constraints.Positive() private Long departmentId;

  private String doctorName;

  private LocalDateTime appointmentAt;

  private String type;

  private String status;

  private String reason;
}
