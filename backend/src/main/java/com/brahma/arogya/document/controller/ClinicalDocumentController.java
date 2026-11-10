package com.brahma.arogya.document.controller;

import com.brahma.arogya.document.dto.ClinicalDocumentIssueRequest;
import com.brahma.arogya.document.service.ClinicalDocumentService;
import com.google.zxing.*;
import java.io.*;
import java.security.Principal;
import java.util.*;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ClinicalDocumentController {

  private final ClinicalDocumentService service;

  public ClinicalDocumentController(ClinicalDocumentService service) {
    this.service = service;
  }

  @GetMapping({"/documents", "/patient/documents"})
  public List<Map<String, Object>> documents(
      Principal p, jakarta.servlet.http.HttpServletRequest req) {
    return service.documents(p, req);
  }

  @PostMapping("/documents/issue")
  public Map<String, Object> issue(
      @RequestBody @jakarta.validation.Valid() ClinicalDocumentIssueRequest f,
      Principal principal) {
    return service.issue(f, principal);
  }

  @GetMapping({"/documents/{id}/pdf", "/patient/documents/{id}/pdf"})
  public ResponseEntity<byte[]> pdf(
      @PathVariable long id, Principal p, jakarta.servlet.http.HttpServletRequest req)
      throws Exception {
    return service.pdf(id, p, req);
  }

  @GetMapping("/public/documents/verify/{token}")
  public Map<String, Object> verify(@PathVariable String token) {
    return service.verify(token);
  }
}
