import { JsonRecord } from "../../core/models/api.models";
export interface ClinicalVisit extends JsonRecord {
  id?: number;
  encounterNo?: string;
  patientId?: number;
  doctorName?: string;
  status?: string;
}
export interface Prescription extends JsonRecord {
  id?: number;
  patient_id?: number;
  status?: string;
}
