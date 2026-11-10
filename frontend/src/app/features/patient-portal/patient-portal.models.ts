import { JsonRecord } from "../../core/models/api.models";
export interface PatientAppointment extends JsonRecord {
  id?: number;
  doctor_name?: string;
  appointment_at?: string;
  status?: string;
}
export interface PatientProfile extends JsonRecord {
  id?: number;
  uhid?: string;
  first_name?: string;
  mobile?: string;
}
