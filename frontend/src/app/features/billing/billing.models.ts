import { JsonRecord } from "../../core/models/api.models";
export interface Invoice extends JsonRecord {
  id?: number;
  invoice_no?: string;
  patient_id?: number;
  total?: number;
  paid?: number;
  patient_outstanding?: number;
  status?: string;
}
