import { JsonRecord } from "../../core/models/api.models";
export interface HelpdeskTicket extends JsonRecord {
  id?: number;
  subject?: string;
  priority?: string;
  status?: string;
}
