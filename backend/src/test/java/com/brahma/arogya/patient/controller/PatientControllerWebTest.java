package com.brahma.arogya.patient.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.brahma.arogya.common.exception.ApiExceptionHandler;
import com.brahma.arogya.patient.entity.Patient;
import com.brahma.arogya.patient.service.PatientService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class PatientControllerWebTest {
  @Test
  void invalidDtoNeverReachesBusinessService() throws Exception {
    var service = mock(PatientService.class);
    var mvc =
        MockMvcBuilders.standaloneSetup(new PatientController(service))
            .setControllerAdvice(new ApiExceptionHandler())
            .build();
    mvc.perform(
            post("/api/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.firstName").exists());
    verifyNoInteractions(service);
  }

  @Test
  void responseDtoPreservesPatientJsonContract() throws Exception {
    var service = mock(PatientService.class);
    var patient = new Patient();
    patient.setId(3L);
    patient.setUhid("MRN-00000003");
    patient.setFirstName("Synthetic");
    when(service.one(3L)).thenReturn(patient);
    var mvc = MockMvcBuilders.standaloneSetup(new PatientController(service)).build();
    mvc.perform(get("/api/patients/3"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(3))
        .andExpect(jsonPath("$.uhid").value("MRN-00000003"))
        .andExpect(jsonPath("$.firstName").value("Synthetic"));
  }
}
