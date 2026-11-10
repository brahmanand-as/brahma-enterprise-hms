package com.brahma.arogya.clinical.dto;

import java.math.*;
import java.time.*;

@lombok.Getter()
@lombok.Setter()
@lombok.NoArgsConstructor()
public class EncounterResponse implements com.brahma.arogya.common.scope.PatientScopedResponse {

  private Long id;

  private Long doctorEmployeeId;

  private String encounterNo;

  private Long patientId;

  private Long appointmentId;

  private Long departmentId;

  private String doctorName;

  private String encounterType;

  private String status;

  private String chiefComplaint;

  private LocalDateTime startedAt;

  private LocalDateTime completedAt;

  @Override
  public Long scopePatientId() {
    return patientId;
  }
}
