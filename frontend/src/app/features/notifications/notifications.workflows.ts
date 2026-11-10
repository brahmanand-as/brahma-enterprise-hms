import {
  WorkflowDefinition,
  Task,
  RowAction,
} from "../../shared/workspace/workspace.models";
import { field, text, numeric, flag } from "../../shared/utils/record.utils";
export const notificationsWorkflows: WorkflowDefinition = {
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
    if (ctx.active === "notifications" && !flag(row, "is_read"))
      add("Mark read", () =>
        ctx.execute(
          "Marked read",
          "notifications/" + numeric(row, "id") + "/read",
        ),
      );
    return actions;
  },
};
