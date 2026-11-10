package com.brahma.arogya.appointment.mapper;

import com.brahma.arogya.appointment.dto.AppointmentRequest;
import com.brahma.arogya.appointment.dto.AppointmentResponse;
import com.brahma.arogya.appointment.entity.Appointment;

public final class AppointmentMapper {

  public static Appointment toEntity(AppointmentRequest value) {
    if (value == null) return null;
    var result = new Appointment();
    result.setId(value.getId());
    result.setDoctorEmployeeId(value.getDoctorEmployeeId());
    result.setPatientId(value.getPatientId());
    result.setDepartmentId(value.getDepartmentId());
    result.setDoctorName(value.getDoctorName());
    result.setAppointmentAt(value.getAppointmentAt());
    result.setType(value.getType());
    result.setStatus(value.getStatus());
    result.setReason(value.getReason());
    return result;
  }

  public static AppointmentResponse toResponse(Appointment value) {
    if (value == null) return null;
    var result = new AppointmentResponse();
    result.setId(value.getId());
    result.setDoctorEmployeeId(value.getDoctorEmployeeId());
    result.setPatientId(value.getPatientId());
    result.setDepartmentId(value.getDepartmentId());
    result.setDoctorName(value.getDoctorName());
    result.setAppointmentAt(value.getAppointmentAt());
    result.setType(value.getType());
    result.setStatus(value.getStatus());
    result.setReason(value.getReason());
    return result;
  }
}
