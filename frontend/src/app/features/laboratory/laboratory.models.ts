import { JsonRecord } from "../../core/models/api.models";
export interface LaboratoryItem extends JsonRecord {
  id?: number;
  patient_id?: number;
  test_name?: string;
  processing_type?: string;
  status?: string;
  sample_barcode?: string | null;
  result_value?: string | null;
}
