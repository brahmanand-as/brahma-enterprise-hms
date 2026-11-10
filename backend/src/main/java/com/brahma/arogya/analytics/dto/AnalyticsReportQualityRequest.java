package com.brahma.arogya.analytics.dto;

import java.io.*;
import java.math.*;
import java.time.*;
import java.util.*;
import java.util.zip.*;

public record AnalyticsReportQualityRequest(
    @jakarta.validation.constraints.Positive() Long encounterId,
    String department,
    String eventType,
    String severity,
    String description) {}
