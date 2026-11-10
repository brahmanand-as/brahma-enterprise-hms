import {
  JsonRecord,
  JsonValue,
  LookupData,
} from "../../core/models/api.models";
import { Field, FieldType } from "../workspace/workspace.models";
export function field(
  key: string,
  label: string,
  source?: string,
  type: FieldType = "text",
  values?: string[],
): Field {
  return { key, label, source, type, values, required: true };
}
export function text(row: JsonRecord, key: string): string {
  const value = row[key];
  return typeof value === "string"
    ? value
    : value === null || value === undefined
      ? ""
      : String(value);
}
export function numeric(row: JsonRecord, key: string): number {
  const value = row[key];
  return typeof value === "number" ? value : Number(value ?? 0);
}
export function flag(row: JsonRecord, key: string): boolean {
  return row[key] === true || row[key] === 1;
}
export function stringList(value: JsonValue | undefined): string[] {
  return Array.isArray(value)
    ? value.filter((item): item is string => typeof item === "string")
    : [];
}
export function asRecord(value: JsonValue | undefined): JsonRecord {
  return value !== null && typeof value === "object" && !Array.isArray(value)
    ? value
    : {};
}
export function asRows(value: JsonRecord | JsonRecord[]): JsonRecord[] {
  return Array.isArray(value) ? value : [value];
}
export function label(key: string): string {
  const names: Record<string, string> = {
    uhid: "MRN",
    firstName: "First name",
    lastName: "Last name",
    patientId: "Patient",
    patient_id: "Patient",
    net_pay: "Net salary",
    reference_no: "Payment reference",
    id: "Reference",
  };
  return (
    names[key] ||
    key
      .replace(/([a-z])([A-Z])/g, "$1 $2")
      .replace(/_/g, " ")
      .replace(/^./, (character) => character.toUpperCase())
  );
}
export function displayValue(
  row: JsonRecord,
  key: string,
  options: LookupData,
): string {
  const value = row[key];
  if (key === "patientId" || key === "patient_id")
    return (
      (options["patients"] || options["dispensePatients"] || []).find(
        (patient) => String(patient.id) === String(value),
      )?.label || (value == null ? "—" : "Patient #" + String(value))
    );
  if (Array.isArray(value)) return value.length + " items";
  if (value === null || value === undefined) return "—";
  if (
    /(_at$|At$|_time$)/.test(key) &&
    typeof value === "string" &&
    !Number.isNaN(Date.parse(value))
  )
    return new Date(value).toLocaleString();
  if (typeof value === "boolean") return value ? "Yes" : "No";
  return String(value);
}
