import {
  WorkflowDefinition,
  Task,
  RowAction,
} from "../../shared/workspace/workspace.models";
import { field, text, numeric, flag } from "../../shared/utils/record.utils";
export const inpatientWorkflows: WorkflowDefinition = {
  tasks(ctx) {
    const f = field,
      t: Task[] = [];
    if (ctx.active === "nursing" && ctx.has("nursing.write"))
      t.push({
        label: "Record quality event",
        path: "reports/quality",
        fields: [
          { ...f("encounterId", "Visit", "encounters"), required: false },
          f("department", "Department", "departmentNames"),
          f("eventType", "Quality indicator"),
          f("severity", "Severity", undefined, "select", [
            "LOW",
            "MODERATE",
            "HIGH",
            "CRITICAL",
          ]),
          f("description", "Event details", undefined, "textarea"),
        ],
      });
    if (ctx.active === "beds" && ctx.has("ipd.manage"))
      t.push({
        label: "Add bed",
        path: "inpatient/beds",
        fields: [
          f("ward", "Ward"),
          f("bedNo", "Bed number"),
          f("floorName", "Floor"),
          f("bedType", "Type", undefined, "select", [
            "GENERAL",
            "PRIVATE",
            "ICU",
            "HDU",
          ]),
          f("dailyRate", "Daily rate", undefined, "number"),
        ],
      });
    if (ctx.active === "admissions" && ctx.has("ipd.manage"))
      t.push({
        label: "Allocate bed",
        path: "operations/ipd/admit",
        fields: [
          f("patientId", "Patient", "patients"),
          f("bedId", "Available / reserved bed", "beds"),
        ],
      });
    if (ctx.active === "recommendations" && ctx.has("ipd.recommend"))
      t.push({
        label: "Recommend admission",
        path: "inpatient/recommendations",
        fields: [
          f("encounterId", "Visit", "encounters"),
          f("wardType", "Care level", undefined, "select", [
            "GENERAL",
            "ICU",
            "HDU",
            "PRIVATE",
          ]),
          f("reason", "Clinical reason"),
        ],
      });
    if (ctx.active === "medication" && ctx.has("prescription.write"))
      t.push({
        label: "Prescribe medication schedule",
        path: "inpatient/medication-orders",
        fields: [
          f("admissionId", "Admission", "admissions"),
          f("inventoryItemId", "Medicine", "inventory"),
          f("dose", "Dose"),
          f("route", "Route", undefined, "select", [
            "ORAL",
            "IV",
            "IM",
            "SC",
            "TOPICAL",
          ]),
          f("startsAt", "First dose", undefined, "datetime-local"),
          f("intervalHours", "Hours between doses", undefined, "number"),
          f("doseCount", "Number of doses", undefined, "number"),
          f("quantityPerDose", "Stock units per dose", undefined, "number"),
        ],
      });
    if (ctx.active === "nursing" && ctx.has("nursing.write"))
      t.push({
        label: "Record care assessment",
        path: "inpatient/nursing-notes",
        fields: [
          f("admissionId", "Admission", "admissions"),
          f("noteType", "Note type", undefined, "select", [
            "ASSESSMENT",
            "HANDOVER",
            "CARE_PLAN",
          ]),
          f("assessment", "Assessment", undefined, "textarea"),
          f("plan", "Care plan", undefined, "textarea"),
        ],
      });
    return t;
  },
  actions(ctx, row) {
    const f = field,
      actions: RowAction[] = [];
    const add = (label: string, run: () => void) =>
      actions.push({ label, run });
    if (
      ctx.active === "admissions" &&
      text(row, "status") === "ADMITTED" &&
      ctx.has("ipd.manage")
    ) {
      add("Transfer", () =>
        ctx.act(
          "Transfer ward / ICU",
          "ward/admissions/" + numeric(row, "id") + "/transfer",
          [f("bedId", "Destination bed", "beds")],
        ),
      );
      add("Discharge", () =>
        ctx.execute(
          "Patient discharged",
          "operations/ipd/admissions/" + numeric(row, "id") + "/discharge",
        ),
      );
    }
    if (
      ctx.active === "recommendations" &&
      text(row, "status") === "PENDING" &&
      ctx.has("ipd.manage")
    )
      add("Link admission", () =>
        ctx.act(
          "Allocate admission request",
          "inpatient/recommendations/" + numeric(row, "id") + "/allocate",
          [f("admissionId", "Allocated admission", "admissions")],
        ),
      );
    if (
      ctx.active === "medication" &&
      text(row, "status") === "ORDERED" &&
      ctx.has("pharmacy.manage")
    )
      add("Verify prescription", () =>
        ctx.execute(
          "Medication verified",
          "inpatient/medication-orders/" + numeric(row, "id") + "/verify",
        ),
      );
    if (
      ctx.active === "emar" &&
      text(row, "status") === "ORDERED" &&
      ctx.has("nursing.write")
    )
      add("Record administration", () =>
        ctx.act(
          "Record medication outcome",
          "ward/medications/" + numeric(row, "id") + "/administer",
          [
            f("status", "Outcome", undefined, "select", [
              "GIVEN",
              "HELD",
              "REFUSED",
              "MISSED",
            ]),
            f("notes", "Nurse notes"),
          ],
        ),
      );
    return actions;
  },
};
