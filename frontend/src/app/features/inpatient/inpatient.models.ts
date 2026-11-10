import { JsonRecord } from "../../core/models/api.models";
export interface Bed extends JsonRecord {
  patient_name?: string | null;
  current_mrn?: string | null;
  id?: number;
  ward?: string;
  bed_no?: string;
  floor_name?: string;
  bed_type?: string;
  status?: string;
  daily_rate?: number;
  occupied?: boolean | number;
}
export interface Admission extends JsonRecord {
  id?: number;
  patientId?: number;
  admissionNo?: string;
  status?: string;
}
