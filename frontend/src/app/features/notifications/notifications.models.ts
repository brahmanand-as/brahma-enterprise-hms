import { JsonRecord } from "../../core/models/api.models";
export interface NotificationRecord extends JsonRecord {
  id?: number;
  subject?: string;
  message?: string;
  is_read?: boolean | number;
  created_at?: string;
}
