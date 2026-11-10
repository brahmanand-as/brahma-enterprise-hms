package com.brahma.arogya.clinical.entity;

import jakarta.persistence.*;
import java.math.*;
import java.time.*;
import lombok.*;

@Entity
@Table(name = "encounters")
@Getter
@Setter
@NoArgsConstructor
public class Encounter {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
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
}
