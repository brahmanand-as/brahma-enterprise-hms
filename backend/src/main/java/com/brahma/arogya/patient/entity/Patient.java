package com.brahma.arogya.patient.entity;

import jakarta.persistence.*;
import java.math.*;
import java.time.*;
import lombok.*;

@Entity
@Table(name = "patients")
@Getter
@Setter
@NoArgsConstructor
public class Patient {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String uhid;

  private String firstName;

  private String lastName;

  private String gender;

  private LocalDate dob;

  private String mobile;

  private String bloodGroup;

  private String address;
}
