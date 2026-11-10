import {
  WorkflowDefinition,
  Task,
  RowAction,
} from "../../shared/workspace/workspace.models";
import { field, text, numeric, flag } from "../../shared/utils/record.utils";
export const patientWorkflows: WorkflowDefinition = {
  tasks(ctx) {
    const f = field,
      t: Task[] = [];
    if (ctx.active === "patients" && ctx.has("patients.write"))
      t.push({
        label: "Register patient",
        path: "patients",
        fields: [
          f("firstName", "First name"),
          f("lastName", "Last name"),
          f("gender", "Gender", undefined, "select", [
            "FEMALE",
            "MALE",
            "OTHER",
          ]),
          f("dob", "Date of birth", undefined, "date"),
          f("mobile", "Mobile"),
          f("address", "Address"),
        ],
      });
    return t;
  },
  actions(ctx, row) {
    const f = field,
      actions: RowAction[] = [];
    const add = (label: string, run: () => void) =>
      actions.push({ label, run });
    if (ctx.active === "patients")
      add("Profile", () => {
        if (ctx.has("clinical.read"))
          ctx.showDetail("clinical/patients/" + numeric(row, "id"));
        else ctx.detail = row;
      });
    if (ctx.active === "patients" && ctx.has("patients.write"))
      add("Edit demographics", () =>
        ctx.act(
          "Save patient demographics",
          "patients/" + numeric(row, "id"),
          [
            f("firstName", "First name"),
            { ...f("lastName", "Last name"), required: false },
            {
              ...f("dob", "Date of birth", undefined, "date"),
              required: false,
            },
            f("mobile", "Mobile"),
            { ...f("address", "Address"), required: false },
          ],
          row,
          row,
          "PUT",
        ),
      );
    return actions;
  },
};
