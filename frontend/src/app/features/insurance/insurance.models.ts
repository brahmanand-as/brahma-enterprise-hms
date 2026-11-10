import { JsonRecord } from "../../core/models/api.models";
export interface InsuranceClaim extends JsonRecord {
  id?: number;
  uhid?: string;
  provider?: string;
  status?: string;
  claim_type?: string;
}
