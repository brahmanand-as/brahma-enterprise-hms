import {
  WorkflowDefinition,
  Task,
  RowAction,
} from "../../shared/workspace/workspace.models";
import { field, text, numeric, flag } from "../../shared/utils/record.utils";
export const billingWorkflows: WorkflowDefinition = {
  tasks(ctx) {
    const f = field,
      t: Task[] = [];
    if (ctx.active === "billing" && ctx.has("billing.manage"))
      t.push({
        label: "Create itemized bill",
        path: "operations/billing/invoices",
        fields: [
          f("patientId", "Patient", "patients"),
          { ...f("encounterId", "Visit", "encounters"), required: false },
          {
            ...f("departmentId", "Billing department", "departments"),
            required: false,
          },
        ],
        lineKey: "items",
        lines: [
          f("description", "Service / item"),
          f("qty", "Quantity", undefined, "number"),
          f("unitPrice", "Unit price", undefined, "number"),
        ],
      });
    return t;
  },
  actions(ctx, row) {
    const f = field,
      actions: RowAction[] = [];
    const add = (label: string, run: () => void) =>
      actions.push({ label, run });
    if (ctx.active === "billing" && ctx.has("billing.manage")) {
      add("Split payment", () =>
        ctx.act(
          "Record verified split payment",
          "finance/invoices/" + numeric(row, "id") + "/payments",
          [],
          {},
          null,
          undefined,
          [
            f("method", "Method", undefined, "select", [
              "CASH",
              "CARD",
              "UPI",
              "BANK",
            ]),
            f("amount", "Amount", undefined, "number"),
            {
              ...f("referenceNo", "Verified transaction reference"),
              required: false,
            },
            {
              ...f("verified", "Payment verified", undefined, "checkbox"),
              required: false,
            },
          ],
          "parts",
        ),
      );
      add("Adjustment", () =>
        ctx.act(
          "Adjust invoice",
          "finance/invoices/" + numeric(row, "id") + "/adjustments",
          [
            f("type", "Type", undefined, "select", [
              "DISCOUNT",
              "CREDIT",
              "TAX",
            ]),
            f("amount", "Amount", undefined, "number"),
            f("reason", "Reason"),
          ],
        ),
      );
      add("Refund", () =>
        ctx.act(
          "Record verified refund",
          "finance/invoices/" + numeric(row, "id") + "/refunds",
          [
            f("amount", "Amount", undefined, "number"),
            f("method", "Method", undefined, "select", [
              "CASH",
              "UPI",
              "CARD",
              "BANK",
            ]),
            f("referenceNo", "Refund reference"),
            f("reason", "Reason"),
          ],
        ),
      );
      add("Statement", () =>
        ctx.showDetail("finance/invoices/" + numeric(row, "id")),
      );
    }
    if (ctx.active === "creditqueue" && text(row, "status") === "PENDING")
      add("Apply credit / refund", () =>
        ctx.act(
          "Reconcile patient return credit",
          "finance/return-credits/" + numeric(row, "id") + "/reconcile",
          [
            {
              ...f("method", "Refund method", undefined, "select", [
                "CASH",
                "UPI",
                "CARD",
                "BANK",
              ]),
              required: false,
            },
            {
              ...f("referenceNo", "Verified refund reference"),
              required: false,
            },
          ],
        ),
      );
    return actions;
  },
};
