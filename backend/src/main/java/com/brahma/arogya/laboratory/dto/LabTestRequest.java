package com.brahma.arogya.laboratory.dto;

import java.math.*;
import java.time.*;

@lombok.Getter()
@lombok.Setter()
@lombok.NoArgsConstructor()
public class LabTestRequest {

  @jakarta.validation.constraints.Positive() private Long id;

  private String code;

  private String name;

  private String sampleType;

  private String defaultProcessingType;

  private BigDecimal price;

  private String resultUnit;

  private String referenceRange;

  private Boolean active;
}
