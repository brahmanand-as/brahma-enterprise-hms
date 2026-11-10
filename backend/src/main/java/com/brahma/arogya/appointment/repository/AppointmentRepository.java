package com.brahma.arogya.appointment.repository;

import com.brahma.arogya.appointment.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {}
