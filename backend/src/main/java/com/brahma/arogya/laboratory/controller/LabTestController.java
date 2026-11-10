package com.brahma.arogya.laboratory.controller;

import com.brahma.arogya.laboratory.dto.LabTestRequest;
import com.brahma.arogya.laboratory.dto.LabTestResponse;
import com.brahma.arogya.laboratory.mapper.LabTestMapper;
import com.brahma.arogya.laboratory.service.LabTestService;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lab-tests")
public class LabTestController {

  private final LabTestService service;

  public LabTestController(LabTestService service) {
    this.service = service;
  }

  @GetMapping
  public List<LabTestResponse> all() {
    return service.all().stream().map(LabTestMapper::toResponse).toList();
  }

  @GetMapping("/{id}")
  public LabTestResponse one(@PathVariable Long id) {
    return LabTestMapper.toResponse(service.one(id));
  }

  @PostMapping
  public LabTestResponse create(@RequestBody @jakarta.validation.Valid() LabTestRequest x) {
    return LabTestMapper.toResponse(service.create(LabTestMapper.toEntity(x)));
  }

  @PutMapping("/{id}")
  public LabTestResponse update(
      @PathVariable Long id, @RequestBody @jakarta.validation.Valid() LabTestRequest x) {
    return LabTestMapper.toResponse(service.update(id, LabTestMapper.toEntity(x)));
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }
}
