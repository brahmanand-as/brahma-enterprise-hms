export type JsonPrimitive = string | number | boolean | null;
export type JsonValue = JsonPrimitive | JsonRecord | JsonValue[];
export interface JsonRecord {
  [key: string]: JsonValue | undefined;
}
export interface ApiProblem {
  detail?: string;
  message?: string;
  errors?: Record<string, string>;
}
export interface CurrentUser {
  username: string;
  role: string;
  permissions: string[];
  roles: string[];
  employeeId: number | null;
}
export interface LoginResponse {
  token: string;
  expiresIn: number;
}
export interface RegistrationRequest {
  firstName: string;
  lastName: string;
  mobile: string;
  username: string;
  password: string;
}
export interface RegistrationResponse {
  mrn: string;
  status: string;
}
export interface LookupOption {
  id: string | number;
  label: string;
  name?: string;
  uhid?: string;
  patient_id?: number;
}
export interface LookupData {
  [source: string]: LookupOption[];
}
export interface Notification extends JsonRecord {
  id: number;
  subject: string;
  message: string;
  is_read: boolean | number;
  created_at: string;
}
export interface Permission extends JsonRecord {
  code: string;
  label: string;
}
export interface DashboardStats {
  patients: number;
  encounters: number;
  labOrders: number;
  invoices: number;
}
