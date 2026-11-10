import {
  WorkflowDefinition,
  Task,
  RowAction,
} from "../../shared/workspace/workspace.models";
import { field, text, numeric, flag } from "../../shared/utils/record.utils";
export const radiologyWorkflows: WorkflowDefinition = {
  tasks(ctx) {
    const f = field,
      t: Task[] = [];
    return t;
  },
  actions(ctx, row) {
    const f = field,
      actions: RowAction[] = [];
    const add = (label: string, run: () => void) =>
      actions.push({ label, run });
    if (ctx.active === "radiology" && ctx.has("radiology.report")) {
      const next: Record<string, string> = {
        ORDERED: "SCHEDULED",
        SCHEDULED: "IN_PROCESS",
        IN_PROCESS: "REPORTED",
        REPORTED: "VERIFIED",
      };
      if (next[text(row, "status")])
        add(ctx.label(next[text(row, "status")]), () =>
          ctx.act(
            "Update radiology",
            "enterprise/radiology/" + numeric(row, "id") + "/transition",
            next[text(row, "status")] === "REPORTED" ||
              next[text(row, "status")] === "VERIFIED"
              ? [f("report", "Verified report", undefined, "textarea")]
              : [],
            { status: next[text(row, "status")] },
          ),
        );
      if (text(row, "status") === "VERIFIED")
        add("Issue report", () =>
          ctx.execute("Radiology report issued", "documents/issue", {
            type: "RADIOLOGY_REPORT",
            referenceId: numeric(row, "id"),
          }),
        );
    }
    return actions;
  },
};
