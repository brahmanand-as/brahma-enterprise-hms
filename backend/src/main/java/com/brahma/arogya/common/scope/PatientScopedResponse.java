package com.brahma.arogya.common.scope;

/** Internal ownership metadata; not part of the HTTP JSON contract. */
public interface PatientScopedResponse {

  @com.fasterxml.jackson.annotation.JsonIgnore
  Long scopePatientId();
}
