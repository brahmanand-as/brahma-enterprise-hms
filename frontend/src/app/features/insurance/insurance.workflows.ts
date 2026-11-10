import {
  WorkflowDefinition,
  Task,
  RowAction,
} from "../../shared/workspace/workspace.models";
import { field, text, numeric, flag } from "../../shared/utils/record.utils";
export const insuranceWorkflows: WorkflowDefinition = {
  tasks(ctx) {
    const f = field,
      t: Task[] = [];
    if (ctx.active === "insurance" && ctx.has("insurance.manage"))
      t.push({
        label: "Open insurance case",
        path: "enterprise/claims",
        fields: [
          f("admissionId", "Admission", "admissions"),
          f("provider", "Provider / TPA"),
          f("policyNo", "Policy number"),
          f("claimType", "Claim type", undefined, "select", [
            "CASHLESS",
            "REIMBURSEMENT",
          ]),
          f("claimAmount", "Claim amount", undefined, "number"),
          f("nonPayable", "Nonpayable amount", undefined, "number"),
        ],
      });
    return t;
  },
  actions(ctx, row) {
    const f = field,
      actions: RowAction[] = [];
    const add = (label: string, run: () => void) =>
      actions.push({ label, run });
    if (ctx.active === "insurance") {
      add("Case details", () =>
        ctx.showDetail("coverage/claims/" + numeric(row, "id")),
      );
      add("Eligibility allocation", () =>
        ctx.act(
          "Allocate eligible / nonpayable items",
          "coverage/claims/" + numeric(row, "id") + "/lines",
          [],
          {},
          null,
          "PUT",
          [
            f("description", "Item / service"),
            f("amount", "Billed amount", undefined, "number"),
            f("eligibleAmount", "Eligible amount", undefined, "number"),
          ],
          "lines",
        ),
      );
      const next: Record<string, string> = {
        DRAFT: "PREAUTH_SUBMITTED",
        PREAUTH_SUBMITTED: "APPROVED",
        APPROVED: "FINAL_SUBMITTED",
      };
      if (next[text(row, "status")])
        add(ctx.label(next[text(row, "status")]), () =>
          ctx.act(
            "Update claim",
            "enterprise/insurance/" + numeric(row, "id") + "/transition",
            [
              f("amount", "Authorized / claimed amount", undefined, "number"),
              f("notes", "TPA decision notes"),
            ],
            { status: next[text(row, "status")] },
          ),
        );
      if (
        ["PREAUTH_SUBMITTED", "FINAL_SUBMITTED"].includes(text(row, "status"))
      )
        add("Record rejection", () =>
          ctx.act(
            "Record insurer rejection",
            "enterprise/insurance/" + numeric(row, "id") + "/transition",
            [f("notes", "Insurer rejection reason")],
            { status: "REJECTED", amount: 0 },
          ),
        );
      if (
        text(row, "status") === "APPROVED" ||
        text(row, "status") === "FINAL_SUBMITTED"
      )
        add("Allocate insurer share", () =>
          ctx.act(
            "Link cashless authorization to bill",
            "coverage/claims/" + numeric(row, "id") + "/allocations",
            [
              f("invoiceId", "Patient invoice", "invoices"),
              f("amount", "Authorized insurer share", undefined, "number"),
            ],
          ),
        );
      if (text(row, "status") === "APPROVED")
        add("Enhancement", () =>
          ctx.act(
            "Request enhancement",
            "coverage/claims/" + numeric(row, "id") + "/enhancements",
            [
              f("amount", "Additional authorization", undefined, "number"),
              f("notes", "Clinical reason"),
            ],
          ),
        );
      if (
        ["FINAL_SUBMITTED", "PARTIALLY_SETTLED"].includes(text(row, "status"))
      )
        add("Reconcile settlement", () =>
          ctx.act(
            "Record insurer settlement",
            "coverage/claims/" + numeric(row, "id") + "/settlements",
            [
              f("amount", "Received amount", undefined, "number"),
              f("referenceNo", "Settlement bank reference"),
              f("receivedOn", "Received date", undefined, "date"),
            ],
          ),
        );
    }
    if (ctx.active === "enhancements" && text(row, "status") === "PENDING")
      add("Review enhancement", () =>
        ctx.act(
          "Review enhancement request",
          "coverage/enhancements/" + numeric(row, "id") + "/review",
          [
            f("status", "Decision", undefined, "select", [
              "APPROVED",
              "REJECTED",
            ]),
            f("amount", "Approved additional amount", undefined, "number"),
            f("notes", "TPA decision notes"),
          ],
        ),
      );
    return actions;
  },
};
