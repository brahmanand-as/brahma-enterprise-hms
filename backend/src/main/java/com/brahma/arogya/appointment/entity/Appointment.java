package com.brahma.arogya.appointment.entity;

import jakarta.persistence.*;
import java.math.*;
import java.time.*;
import lombok.*;

@Entity
@Table(name = "appointments")
@Getter
@Setter
@NoArgsConstructor
public class Appointment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long doctorEmployeeId;

  private Long patientId;

  private Long departmentId;

  private String doctorName;

  private LocalDateTime appointmentAt;

  private String type;

  private String status;

  private String reason;
}
