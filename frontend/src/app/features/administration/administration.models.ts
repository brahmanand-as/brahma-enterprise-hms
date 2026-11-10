import { JsonRecord } from "../../core/models/api.models";
export interface Department extends JsonRecord {
  id?: number;
  code?: string;
  name?: string;
}
export interface EnterpriseRole extends JsonRecord {
  name?: string;
  label?: string;
  system_role?: boolean;
  permissions?: string[];
}
