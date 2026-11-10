import { JsonRecord } from "../../core/models/api.models";
export interface IssuedDocument extends JsonRecord {
  id?: number;
  document_type?: string;
  patient_id?: number;
  issued_by?: string;
  issued_at?: string;
}
