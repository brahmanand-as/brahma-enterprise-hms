import { JsonRecord } from "../../core/models/api.models";
export interface Appointment extends JsonRecord {
  id?: number;
  patientId?: number;
  doctorName?: string;
  appointmentAt?: string;
  status?: string;
}
export interface DoctorSlot extends JsonRecord {
  id?: number;
  doctor_name?: string;
  starts_at?: string;
  ends_at?: string;
}
