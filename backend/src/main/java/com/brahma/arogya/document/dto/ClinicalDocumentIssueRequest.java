package com.brahma.arogya.document.dto;

import com.google.zxing.*;
import java.io.*;
import java.util.*;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.*;

public record ClinicalDocumentIssueRequest(
    @jakarta.validation.constraints.NotBlank() String type,
    @jakarta.validation.constraints.Positive() @jakarta.validation.constraints.NotNull()
        Long referenceId) {}
