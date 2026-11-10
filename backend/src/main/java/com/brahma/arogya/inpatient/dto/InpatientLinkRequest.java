package com.brahma.arogya.inpatient.dto;

import java.math.*;
import java.time.*;
import java.util.*;

public record InpatientLinkRequest(@jakarta.validation.constraints.Positive() Long admissionId) {}
