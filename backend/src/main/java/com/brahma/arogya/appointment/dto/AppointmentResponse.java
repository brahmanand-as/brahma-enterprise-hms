package com.brahma.arogya.appointment.dto;

import java.math.*;
import java.time.*;

@lombok.Getter()
@lombok.Setter()
@lombok.NoArgsConstructor()
public class AppointmentResponse implements com.brahma.arogya.common.scope.PatientScopedResponse {

  private Long id;

  private Long doctorEmployeeId;

  private Long patientId;

  private Long departmentId;

  private String doctorName;

  private LocalDateTime appointmentAt;

  private String type;

  private String status;

  private String reason;

  @Override
  public Long scopePatientId() {
    return patientId;
  }
}
