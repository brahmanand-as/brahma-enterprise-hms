import { JsonRecord } from "../../core/models/api.models";
export interface Employee extends JsonRecord {
  id?: number;
  employeeNo?: string;
  name?: string;
  department?: string;
  designation?: string;
  status?: string;
}
export interface RosterShift extends JsonRecord {
  id?: number;
  employee_id?: number;
  shift_date?: string;
  starts_at?: string;
  ends_at?: string;
  status?: string;
}
