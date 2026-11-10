import {
  WorkflowDefinition,
  Task,
  RowAction,
} from "../../shared/workspace/workspace.models";
import { field, text, numeric, flag } from "../../shared/utils/record.utils";
export const helpdeskWorkflows: WorkflowDefinition = {
  tasks(ctx) {
    const f = field,
      t: Task[] = [];
    if (ctx.active === "helpdesk")
      t.push({
        label: "Open helpdesk ticket",
        path: "helpdesk/tickets",
        fields: [
          f("subject", "Subject"),
          f("description", "How can we help?", undefined, "textarea"),
          f("priority", "Priority", undefined, "select", [
            "LOW",
            "NORMAL",
            "HIGH",
            "URGENT",
          ]),
        ],
      });
    return t;
  },
  actions(ctx, row) {
    const f = field,
      actions: RowAction[] = [];
    const add = (label: string, run: () => void) =>
      actions.push({ label, run });
    if (ctx.active === "helpdesk") {
      add("Conversation", () => {
        ctx.selected = row;
        ctx.showDetail("helpdesk/tickets/" + numeric(row, "id") + "/messages");
      });
      add("Add message", () =>
        ctx.act(
          "Send ticket update",
          "helpdesk/tickets/" + numeric(row, "id") + "/messages",
          [f("message", "Message", undefined, "textarea")],
        ),
      );
      if (ctx.has("helpdesk.manage"))
        add("Assign / resolve", () =>
          ctx.act(
            "Update helpdesk ticket",
            "helpdesk/tickets/" + numeric(row, "id") + "/review",
            [
              f("status", "Status", undefined, "select", [
                "ASSIGNED",
                "IN_PROGRESS",
                "ESCALATED",
                "RESOLVED",
                "CLOSED",
              ]),
              f("assignedTo", "Assigned staff", "helpdeskStaff"),
              f("department", "Routing department"),
              f("resolution", "Resolution / progress", undefined, "textarea"),
            ],
          ),
        );
    }
    return actions;
  },
};
