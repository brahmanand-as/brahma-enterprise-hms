import { JsonRecord } from "../../core/models/api.models";
export interface RadiologyOrder extends JsonRecord {
  id?: number;
  patientId?: number;
  studyName?: string;
  status?: string;
}
