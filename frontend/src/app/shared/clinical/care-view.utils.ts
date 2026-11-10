import { JsonRecord, LookupData } from "../../core/models/api.models";
import { text, displayValue } from "../utils/record.utils";
// API LocalDateTime values are hospital wall time (Asia/Kolkata).
export function hospitalTime(value: unknown): number {
  if (typeof value !== "string" || !value) return NaN;
  return Date.parse(
    /(?:Z|[+-]\d{2}:\d{2})$/.test(value) ? value : value + "+05:30",
  );
}
export function hospitalDay(value: unknown = new Date().toISOString()): string {
  const time = hospitalTime(value);
  if (!Number.isFinite(time)) return "";
  return new Date(time + 330 * 60000).toISOString().slice(0, 10);
}
export function elapsedMinutes(
  value: unknown,
  now = Date.now(),
): number | null {
  const start = hospitalTime(value);
  return Number.isFinite(start)
    ? Math.max(0, Math.floor((now - start) / 60000))
    : null;
}
export function patientLabel(row: JsonRecord, options: LookupData): string {
  return text(row, "patient_name") || displayValue(row, "patientId", options);
}
export function initials(value: string): string {
  const clean = value.split("·").pop()?.trim() || value;
  return clean
    .split(/\s+/)
    .slice(0, 2)
    .map((s) => s[0] || "")
    .join("")
    .toUpperCase();
}
export function isWaiting(row: JsonRecord): boolean {
  return ["CHECKED_IN", "WAITING"].includes(text(row, "status"));
}
export function queueRows(
  rows: JsonRecord[],
  day: string,
  doctor: string,
): JsonRecord[] {
  return rows
    .filter(
      (row) =>
        (!day || hospitalDay(row["startedAt"]) === day) &&
        (!doctor || text(row, "doctorName") === doctor),
    )
    .sort(
      (a, b) =>
        (hospitalTime(a["startedAt"]) || 0) -
        (hospitalTime(b["startedAt"]) || 0),
    );
}
export function waitingAverage(
  rows: JsonRecord[],
  now = Date.now(),
): number | null {
  const values = rows
    .filter(isWaiting)
    .map((r) => elapsedMinutes(r["startedAt"], now))
    .filter((v): v is number => v !== null);
  return values.length
    ? Math.round(values.reduce((a, b) => a + b, 0) / values.length)
    : null;
}
