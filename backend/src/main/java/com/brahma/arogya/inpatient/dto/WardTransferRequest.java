package com.brahma.arogya.inpatient.dto;

import java.time.*;
import java.util.*;

public record WardTransferRequest(@jakarta.validation.constraints.Positive() Long bedId) {}
