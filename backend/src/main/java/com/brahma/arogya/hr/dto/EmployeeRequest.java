package com.brahma.arogya.hr.dto;

import java.math.*;
import java.time.*;

@lombok.Getter()
@lombok.Setter()
@lombok.NoArgsConstructor()
public class EmployeeRequest {

  @jakarta.validation.constraints.Positive() private Long id;

  private String employeeNo;

  private String name;

  private String department;

  private String designation;

  private String mobile;

  private String status;
}
