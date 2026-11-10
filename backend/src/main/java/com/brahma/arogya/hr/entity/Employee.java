package com.brahma.arogya.hr.entity;

import jakarta.persistence.*;
import java.math.*;
import java.time.*;
import lombok.*;

@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
public class Employee {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String employeeNo;

  private String name;

  private String department;

  private String designation;

  private String mobile;

  private String status;
}
