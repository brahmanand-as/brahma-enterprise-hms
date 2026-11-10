import {
  WorkflowDefinition,
  Task,
  RowAction,
} from "../../shared/workspace/workspace.models";
import { field, text, numeric, flag } from "../../shared/utils/record.utils";
export const administrationWorkflows: WorkflowDefinition = {
  tasks(ctx) {
    const f = field,
      t: Task[] = [];
    if (ctx.active === "departments")
      t.push({
        label: "Add hospital department",
        path: "masters/departments",
        fields: [f("code", "Department code"), f("name", "Department name")],
      });
    if (ctx.active === "careteams" && ctx.has("care.assign"))
      t.push({
        label: "Assign care team",
        path: "care/teams",
        fields: [
          f("patientId", "Patient", "patients"),
          f("username", "Clinical staff", "clinicalStaff"),
        ],
      });
    if (ctx.active === "roles" && ctx.has("security.manage"))
      t.push({
        label: "Create custom role",
        path: "security/roles",
        fields: [
          f("name", "Role code, e.g. OPD_COORDINATOR"),
          f("label", "Display name"),
        ],
      });
    return t;
  },
  actions(ctx, row) {
    const f = field,
      actions: RowAction[] = [];
    const add = (label: string, run: () => void) =>
      actions.push({ label, run });
    if (ctx.active === "roles" && !flag(row, "system_role"))
      add("Edit permissions", () =>
        ctx.act(
          "Save role permissions",
          "security/roles/" + text(row, "name"),
          [],
          {},
          row,
          "PUT",
        ),
      );
    if (ctx.active === "accounts")
      add("Manage account", () =>
        ctx.act(
          "Update account",
          "security/accounts/" + numeric(row, "id"),
          [
            { ...f("role", "Role", "roles"), required: false },
            {
              ...f("enabled", "Enabled", undefined, "checkbox"),
              required: false,
            },
            {
              ...f("password", "Reset password", undefined, "password"),
              required: false,
            },
          ],
          { enabled: flag(row, "enabled"), role: text(row, "role") },
          row,
          "PATCH",
        ),
      );
    if (ctx.active === "settings")
      add("Edit", () =>
        ctx.act(
          "Save setting",
          "security/settings/" + text(row, "setting_key"),
          [f("value", "Setting value")],
          { value: row["setting_value"] },
          row,
          "PUT",
        ),
      );
    return actions;
  },
};
