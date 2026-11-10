import {
  WorkflowDefinition,
  Task,
  RowAction,
} from "../../shared/workspace/workspace.models";
import { field, text, numeric, flag } from "../../shared/utils/record.utils";
export const clinicalWorkflows: WorkflowDefinition = {
  tasks(ctx) {
    const f = field,
      t: Task[] = [];
    if (ctx.active === "visits") {
      if (ctx.has("clinical.checkin"))
        t.push({
          label: "Register walk-in",
          path: "workflow/walk-in",
          fields: [
            f("patientId", "Patient", "patients"),
            f("doctorEmployeeId", "Doctor", "doctors"),
            f("chiefComplaint", "Chief complaint"),
          ],
        });
      if (ctx.has("lab.order"))
        t.push({
          label: "Order laboratory tests",
          path: "operations/lab/orders",
          fields: [f("encounterId", "Clinical visit", "encounters")],
          lineKey: "tests",
          lines: [
            f("testCode", "Test", "labTests"),
            f("processingType", "Processing", undefined, "select", [
              "IN_HOUSE",
              "OUTSOURCE",
            ]),
            {
              ...f("externalLabId", "External lab", "externalLabs"),
              required: false,
            },
          ],
        });
      if (ctx.has("radiology.order"))
        t.push({
          label: "Order radiology",
          path: "orders/radiology",
          fields: [
            f("encounterId", "Clinical visit", "encounters"),
            f("studyName", "Study"),
          ],
        });
      if (ctx.has("prescription.write"))
        t.push({
          label: "Create prescription",
          path: "orders/prescriptions",
          fields: [f("encounterId", "Clinical visit", "encounters")],
          lineKey: "items",
          lines: [
            f("medicineName", "Medicine", "medicineNames"),
            f("dose", "Dose"),
            f("frequency", "Frequency"),
            f("duration", "Duration"),
            { ...f("instructions", "Instructions"), required: false },
          ],
        });
    }
    return t;
  },
  actions(ctx, row) {
    const f = field,
      actions: RowAction[] = [];
    const add = (label: string, run: () => void) =>
      actions.push({ label, run });
    if (ctx.active === "visits") {
      if (
        ctx.has("clinical.vitals") &&
        !["COMPLETED", "CANCELLED"].includes(text(row, "status"))
      )
        add("Record vitals", () =>
          ctx.act(
            "Record vitals",
            "workflow/encounters/" + numeric(row, "id") + "/vitals",
            [
              f("temperature", "Temperature °C", undefined, "number"),
              f("pulse", "Pulse", undefined, "number"),
              f("systolic", "Systolic", undefined, "number"),
              f("diastolic", "Diastolic", undefined, "number"),
              f("spo2", "SpO₂", undefined, "number"),
              f("weightKg", "Weight kg", undefined, "number"),
            ],
          ),
        );
      if (ctx.has("clinical.consult")) {
        if (["CHECKED_IN", "WAITING"].includes(text(row, "status")))
          add("Start consultation", () =>
            ctx.execute(
              "Consultation started",
              "workflow/encounters/" +
                numeric(row, "id") +
                "/start-consultation",
            ),
          );
        if (text(row, "status") === "IN_CONSULTATION") {
          add("Clinical notes", () =>
            ctx.act(
              "Save consultation",
              "workflow/encounters/" + numeric(row, "id") + "/consultation",
              [
                f("history", "History", undefined, "textarea"),
                f("examination", "Examination", undefined, "textarea"),
                f("diagnosis", "Diagnosis", undefined, "textarea"),
                f("advice", "Advice", undefined, "textarea"),
              ],
            ),
          );
          add("Sign & send to billing", () =>
            ctx.execute(
              "Signed clinical visit sent to billing",
              "workflow/encounters/" + numeric(row, "id") + "/billing",
            ),
          );
        }
      }
    }
    if (ctx.active === "prescriptions") {
      if (text(row, "status") === "DRAFT" && ctx.has("prescription.write"))
        add("Finalize", () =>
          ctx.execute(
            "Prescription finalized",
            "orders/prescriptions/" + numeric(row, "id") + "/finalize",
          ),
        );
      if (text(row, "status") === "FINAL" && ctx.has("documents.read"))
        add("Issue PDF", () =>
          ctx.execute("Prescription issued", "documents/issue", {
            type: "PRESCRIPTION",
            referenceId: numeric(row, "id"),
          }),
        );
    }
    return actions;
  },
};
