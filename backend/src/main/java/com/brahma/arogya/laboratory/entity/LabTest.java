package com.brahma.arogya.laboratory.entity;

import jakarta.persistence.*;
import java.math.*;
import java.time.*;
import lombok.*;

@Entity
@Table(name = "lab_test_catalog")
@Getter
@Setter
@NoArgsConstructor
public class LabTest {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String code;

  private String name;

  private String sampleType;

  private String defaultProcessingType;

  private BigDecimal price;

  private String resultUnit;

  private String referenceRange;

  private Boolean active;
}
