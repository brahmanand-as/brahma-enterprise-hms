package com.brahma.arogya.patient.dto;

import java.math.*;
import java.time.*;

@lombok.Getter()
@lombok.Setter()
@lombok.NoArgsConstructor()
public class PatientRequest {

  @jakarta.validation.constraints.Positive() private Long id;

  private String uhid;

  @jakarta.validation.constraints.NotBlank() private String firstName;

  private String lastName;

  private String gender;

  private LocalDate dob;

  private String mobile;

  private String bloodGroup;

  private String address;
}
