package com.brahma.arogya.clinical.dto;

import java.math.*;
import java.time.*;

@lombok.Getter()
@lombok.Setter()
@lombok.NoArgsConstructor()
public class EncounterRequest {

  @jakarta.validation.constraints.Positive() private Long id;

  @jakarta.validation.constraints.Positive() private Long doctorEmployeeId;

  private String encounterNo;

  @jakarta.validation.constraints.Positive() private Long patientId;

  @jakarta.validation.constraints.Positive() private Long appointmentId;

  @jakarta.validation.constraints.Positive() private Long departmentId;

  private String doctorName;

  private String encounterType;

  private String status;

  private String chiefComplaint;

  private LocalDateTime startedAt;

  private LocalDateTime completedAt;
}
