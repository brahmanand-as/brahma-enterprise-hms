package com.brahma.arogya.common.exception;

import org.springframework.dao.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ProblemDetail> conflict(DataIntegrityViolationException e) {
    return ResponseEntity.status(409)
        .body(
            ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "Record is linked or a unique value already exists. Retain clinical history and"
                    + " deactivate linked records."));
  }

  @ExceptionHandler(EmptyResultDataAccessException.class)
  public ResponseEntity<ProblemDetail> missing(EmptyResultDataAccessException e) {
    return ResponseEntity.status(404)
        .body(ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Record not found"));
  }

  @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
  public ResponseEntity<ProblemDetail> validation(
      org.springframework.web.bind.MethodArgumentNotValidException e) {
    var fields = new java.util.LinkedHashMap<String, String>();
    e.getBindingResult()
        .getFieldErrors()
        .forEach(error -> fields.putIfAbsent(error.getField(), error.getDefaultMessage()));
    var problem =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Request validation failed: "
                + String.join(
                    "; ",
                    fields.entrySet().stream()
                        .map(field -> field.getKey() + " " + field.getValue())
                        .toList()));
    problem.setProperty("errors", fields);
    return ResponseEntity.badRequest().body(problem);
  }
}
