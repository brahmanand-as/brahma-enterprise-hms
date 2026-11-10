import { JsonRecord } from "../../core/models/api.models";
export interface Patient extends JsonRecord {
  id?: number;
  uhid?: string;
  firstName?: string;
  lastName?: string;
  mobile?: string;
}
