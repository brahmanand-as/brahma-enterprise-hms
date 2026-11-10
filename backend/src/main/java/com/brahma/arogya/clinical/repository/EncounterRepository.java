package com.brahma.arogya.clinical.repository;

import com.brahma.arogya.clinical.entity.Encounter;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EncounterRepository extends JpaRepository<Encounter, Long> {

  public Optional<Encounter> findByAppointmentId(Long appointmentId);
}
