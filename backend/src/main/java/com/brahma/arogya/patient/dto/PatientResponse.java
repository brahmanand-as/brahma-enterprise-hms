package com.brahma.arogya.patient.dto;

import java.math.*;
import java.time.*;

@lombok.Getter()
@lombok.Setter()
@lombok.NoArgsConstructor()
public class PatientResponse implements com.brahma.arogya.common.scope.PatientScopedResponse {

  private Long id;

  private String uhid;

  private String firstName;

  private String lastName;

  private String gender;

  private LocalDate dob;

  private String mobile;

  private String bloodGroup;

  private String address;

  @Override
  public Long scopePatientId() {
    return id;
  }
}
