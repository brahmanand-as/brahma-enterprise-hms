import { Injectable } from "@angular/core";
import { Observable, forkJoin, map } from "rxjs";
import { ApiClient } from "../http/api-client.service";
import { SessionStore } from "../auth/session.store";
import { JsonRecord, LookupData, LookupOption } from "../models/api.models";
import { Task } from "../../shared/workspace/workspace.models";
import { asRecord, numeric, text } from "../../shared/utils/record.utils";
@Injectable({ providedIn: "root" })
export class LookupService {
  constructor(
    private readonly api: ApiClient,
    private readonly session: SessionStore,
  ) {}
  base(): Observable<LookupData> {
    return this.api.get<LookupData>("portal/options").pipe(
      map((data) => {
        if (data["patients"])
          data["patients"] = data["patients"].map((option) => ({
            ...option,
            label: option.uhid
              ? option.uhid + " · " + option.label
              : option.label,
          }));
        return data;
      }),
    );
  }
  forTask(task: Task): Observable<LookupData> {
    const sources = new Set(
      [...task.fields, ...(task.lines || [])].map((field) => field.source),
    );
    const requests: Observable<LookupData>[] = [this.base()];
    if (sources.has("slots"))
      requests.push(
        this.api
          .get<JsonRecord[]>("public/slots")
          .pipe(
            map((rows) => ({
              slots: rows.map((row) => ({
                id: numeric(row, "id"),
                label:
                  text(row, "doctor_name") +
                  " · " +
                  new Date(text(row, "starts_at")).toLocaleString(),
              })),
            })),
          ),
      );
    if (sources.has("suppliers"))
      requests.push(
        this.api
          .get<JsonRecord[]>("pharmacy/suppliers")
          .pipe(
            map((rows) => ({
              suppliers: rows.map((row) => ({
                id: numeric(row, "id"),
                label: text(row, "name"),
              })),
            })),
          ),
      );
    if (sources.has("purchaseLines"))
      requests.push(
        this.api.get<JsonRecord[]>("pharmacy/purchases").pipe(
          map((rows) => ({
            purchaseLines: rows.flatMap((row) =>
              (Array.isArray(row["lines"]) ? row["lines"] : []).map((value) => {
                const line = asRecord(value);
                return {
                  id: numeric(line, "id"),
                  label:
                    "#" +
                    numeric(row, "id") +
                    " · " +
                    text(line, "name") +
                    " · Remaining " +
                    (numeric(line, "quantity") - numeric(line, "received")),
                };
              }),
            ),
          })),
        ),
      );
    if (
      this.session.has("pharmacy.read") &&
      (sources.has("prescriptions") || sources.has("dispensePatients"))
    )
      requests.push(
        this.api
          .get<JsonRecord[]>("orders/prescriptions")
          .pipe(
            map((rows) => ({
              prescriptions: rows
                .filter((row) => row["status"] === "FINAL")
                .map((row) => ({
                  id: numeric(row, "id"),
                  label:
                    "Prescription #" +
                    numeric(row, "id") +
                    " · Patient " +
                    numeric(row, "patient_id"),
                })),
              dispensePatients: this.patients(rows),
            })),
          ),
      );
    if (
      this.session.has("pharmacy.read") &&
      (sources.has("medications") || sources.has("dispensePatients"))
    )
      requests.push(
        this.api
          .get<JsonRecord[]>("inpatient/medication-orders")
          .pipe(
            map((rows) => ({
              medications: rows
                .filter((row) => row["status"] === "VERIFIED")
                .map((row) => ({
                  id: numeric(row, "id"),
                  label: text(row, "uhid") + " · " + text(row, "medicine_name"),
                })),
              dispensePatients: this.patients(rows),
            })),
          ),
      );
    return forkJoin(requests).pipe(
      map((parts) => {
        const merged: LookupData = {};
        for (const part of parts)
          for (const [key, options] of Object.entries(part)) {
            const combined =
              key === "dispensePatients"
                ? [...(merged[key] || []), ...options]
                : options;
            merged[key] = combined.filter(
              (option, index) =>
                combined.findIndex((other) => other.id === option.id) === index,
            );
          }
        return merged;
      }),
    );
  }
  private patients(rows: JsonRecord[]): LookupOption[] {
    return rows.map((row) => ({
      id: numeric(row, "patient_id"),
      label: text(row, "patient_name")
        ? text(row, "uhid") + " · " + text(row, "patient_name")
        : text(row, "uhid") || "Patient " + numeric(row, "patient_id"),
    }));
  }
}
