import {
  WorkflowDefinition,
  Task,
  RowAction,
} from "../../shared/workspace/workspace.models";
import { field, text, numeric, flag } from "../../shared/utils/record.utils";
export const patientPortalWorkflows: WorkflowDefinition = {
  tasks(ctx) {
    const f = field,
      t: Task[] = [];
    if (ctx.active === "myprofile")
      t.push({
        label: "Update contact details",
        path: "patient/profile",
        method: "PUT",
        fields: [
          f("mobile", "Mobile"),
          f("address", "Address"),
          { ...f("bloodGroup", "Blood group"), required: false },
        ],
      });
    if (["appointments", "myappointments"].includes(ctx.active)) {
      if (ctx.active === "myappointments" || ctx.has("appointments.manage"))
        t.push({
          label: "Book appointment",
          path: ctx.role === "PATIENT" ? "patient/book" : "scheduling/book",
          fields: [
            ...(ctx.role === "PATIENT"
              ? []
              : [f("patientId", "Patient", "patients")]),
            f("slotId", "Doctor and available time", "slots"),
            f("reason", "Reason for visit"),
          ],
        });
      if (ctx.has("appointments.manage"))
        t.push({
          label: "Reception provisional booking",
          path: "scheduling/provisional",
          fields: [
            f("patientId", "Patient", "patients"),
            f("doctorName", "Doctor name"),
            f("reason", "Reception notes"),
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
    if (
      ["appointments", "myappointments"].includes(ctx.active) &&
      ["BOOKED", "PENDING"].includes(text(row, "status"))
    ) {
      const prefix = ctx.role === "PATIENT" ? "patient" : "scheduling";
      if (ctx.has("clinical.checkin") && text(row, "status") === "BOOKED")
        add("Check in", () =>
          ctx.execute(
            "Patient checked in",
            "workflow/appointments/" + numeric(row, "id") + "/check-in",
          ),
        );
      if (ctx.role === "PATIENT" || ctx.has("appointments.manage")) {
        add("Reschedule", () =>
          ctx.act(
            "Reschedule appointment",
            prefix + "/appointments/" + numeric(row, "id") + "/reschedule",
            [f("slotId", "New available slot", "slots")],
          ),
        );
        add("Cancel", () =>
          ctx.act(
            "Cancel appointment",
            prefix + "/appointments/" + numeric(row, "id") + "/cancel",
            [f("reason", "Cancellation reason")],
          ),
        );
      }
      if (ctx.role === "PATIENT")
        add("Ask reception", () =>
          ctx.act(
            "Request scheduling assistance",
            "patient/appointments/" +
              numeric(row, "id") +
              "/reschedule-request",
            [f("reason", "Preferred date / reason")],
          ),
        );
    }
    if (["documents", "mydocuments"].includes(ctx.active))
      add("Preview / print PDF", () =>
        ctx.download(
          (ctx.role === "PATIENT" ? "patient/" : "") +
            "documents/" +
            numeric(row, "id") +
            "/pdf",
          true,
        ),
      );
    return actions;
  },
};
