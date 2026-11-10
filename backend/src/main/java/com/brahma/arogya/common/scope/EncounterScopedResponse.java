package com.brahma.arogya.common.scope;

/** Internal ownership metadata; not part of the HTTP JSON contract. */
public interface EncounterScopedResponse {

  @com.fasterxml.jackson.annotation.JsonIgnore
  Long scopeEncounterId();
}
