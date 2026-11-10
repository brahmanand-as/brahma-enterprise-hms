import {
  WorkflowDefinition,
  Task,
  RowAction,
} from "../../shared/workspace/workspace.models";
import { field, text, numeric, flag } from "../../shared/utils/record.utils";
export const laboratoryWorkflows: WorkflowDefinition = {
  tasks(ctx) {
    const f = field,
      t: Task[] = [];
    if (ctx.active === "labcatalog")
      t.push({
        label: "Add laboratory test",
        path: "lab-tests",
        fields: [
          f("code", "Test code"),
          f("name", "Test name"),
          f("sampleType", "Specimen"),
          f("defaultProcessingType", "Processing", undefined, "select", [
            "IN_HOUSE",
            "OUTSOURCE",
          ]),
          f("price", "Price", undefined, "number"),
          { ...f("resultUnit", "Result unit"), required: false },
          {
            ...f("referenceRange", "Reference range template"),
            required: false,
          },
        ],
      });
    return t;
  },
  actions(ctx, row) {
    const f = field,
      actions: RowAction[] = [];
    const add = (label: string, run: () => void) =>
      actions.push({ label, run });
    if (ctx.active === "laboratory") {
      const next: Record<string, string> = {
        ORDERED: "BILLED",
        BILLED: "SAMPLE_COLLECTED",
        SAMPLE_COLLECTED: "IN_PROCESS",
        IN_PROCESS: "RESULT_ENTERED",
        RESULT_ENTERED: "VERIFIED",
        VERIFIED: "COMPLETED",
      };
      const n = next[text(row, "status")];
      const permission =
        n === "VERIFIED"
          ? "lab.verify"
          : n === "COMPLETED"
            ? "lab.complete"
            : "lab.process";
      if (n && ctx.has(permission))
        add(ctx.label(n), () =>
          ctx.act(
            "Advance laboratory test",
            "operations/lab/items/" + numeric(row, "id") + "/transition",
            n === "RESULT_ENTERED"
              ? [
                  f("resultValue", "Result"),
                  f("resultUnit", "Unit"),
                  f("referenceRange", "Reference range"),
                ]
              : [],
            { status: n, verifiedBy: ctx.username },
          ),
        );
      if (
        ["VERIFIED", "COMPLETED"].includes(text(row, "status")) &&
        ctx.has("documents.read")
      )
        add("Issue report", () =>
          ctx.execute("Report issued", "documents/issue", {
            type: "LAB_REPORT",
            referenceId: numeric(row, "id"),
          }),
        );
    }
    if (
      ctx.active === "laboratory" &&
      text(row, "processing_type") === "OUTSOURCE" &&
      text(row, "status") === "SAMPLE_COLLECTED" &&
      ctx.has("lab.process")
    )
      add("Dispatch specimen", () =>
        ctx.act(
          "Dispatch outsourced sample",
          "operations/lab/dispatches",
          [
            f("sampleId", "Specimen barcode"),
            f("courierDetails", "Courier service"),
            f("trackingNumber", "Tracking number"),
          ],
          {
            labOrderItemId: numeric(row, "id"),
            sampleId: row["sample_barcode"],
          },
        ),
      );
    if (ctx.active === "dispatches" && text(row, "status") === "DISPATCHED")
      add("Confirm receipt", () =>
        ctx.execute(
          "External laboratory receipt confirmed",
          "operations/lab/dispatches/" + numeric(row, "id") + "/receive",
        ),
      );
    return actions;
  },
};
