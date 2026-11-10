import {
  WorkflowDefinition,
  Task,
  RowAction,
} from "../../shared/workspace/workspace.models";
import { field, text, numeric, flag } from "../../shared/utils/record.utils";
export const documentsWorkflows: WorkflowDefinition = {
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
