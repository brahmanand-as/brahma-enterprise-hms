import { JsonRecord } from "../../core/models/api.models";
export interface StockItem extends JsonRecord {
  id?: number;
  sku?: string;
  name?: string;
  stockQty?: number;
}
export interface Dispense extends JsonRecord {
  id?: number;
  patient_id?: number;
  patient_name?: string;
  quantity?: number;
  returned?: number;
  charge_amount?: number;
}
