import {
  WorkflowDefinition,
  Task,
  RowAction,
} from "../../shared/workspace/workspace.models";
import { field, text, numeric, flag } from "../../shared/utils/record.utils";
export const pharmacyWorkflows: WorkflowDefinition = {
  tasks(ctx) {
    const f = field,
      t: Task[] = [];
    if (ctx.active === "inventory" && ctx.has("inventory.manage"))
      t.push({
        label: "Add catalogue item",
        path: "inventory",
        defaults: { stockQty: 0 },
        fields: [
          f("sku", "Stock code"),
          f("name", "Medicine / item name"),
          f("category", "Category"),
          f("unit", "Unit"),
          f("reorderLevel", "Reorder level", undefined, "number"),
        ],
      });
    if (ctx.active === "suppliers" && ctx.has("pharmacy.manage"))
      t.push({
        label: "Add supplier",
        path: "pharmacy/suppliers",
        fields: [f("name", "Supplier name"), f("contact", "Contact")],
      });
    if (ctx.active === "purchases" && ctx.has("pharmacy.manage"))
      t.push(
        {
          label: "Create purchase order",
          path: "pharmacy/purchases",
          fields: [f("supplierId", "Supplier", "suppliers")],
          lineKey: "lines",
          lines: [
            f("inventoryItemId", "Item", "inventory"),
            f("quantity", "Quantity", undefined, "number"),
            f("unitCost", "Unit cost", undefined, "number"),
          ],
        },
        {
          label: "Receive goods",
          path: "pharmacy/receipts",
          fields: [
            f("purchaseLineId", "Purchase line", "purchaseLines"),
            f("batchNo", "Batch number"),
            f("expiresOn", "Expiry", undefined, "date"),
            f("quantity", "Received quantity", undefined, "number"),
            f("salePrice", "Unit sale price", undefined, "number"),
          ],
        },
      );
    if (ctx.active === "dispenses" && ctx.has("pharmacy.manage"))
      t.push({
        label: "Dispense for patient",
        path: "pharmacy/dispenses",
        fields: [
          f("patientId", "Patient", "dispensePatients"),
          {
            ...f("prescriptionId", "Final prescription", "prescriptions"),
            required: false,
          },
          {
            ...f("medicationOrderId", "Verified IPD order", "medications"),
            required: false,
          },
          f("inventoryItemId", "Medicine", "inventory"),
          f("quantity", "Quantity", undefined, "number"),
        ],
      });
    return t;
  },
  actions(ctx, row) {
    const f = field,
      actions: RowAction[] = [];
    const add = (label: string, run: () => void) =>
      actions.push({ label, run });
    if (ctx.active === "batches" && ctx.has("pharmacy.manage"))
      add("Move stock", () =>
        ctx.act(
          "Record stock movement",
          "pharmacy/batches/" +
            numeric(row, "id") +
            "/action?action=" +
            ctx.batchAction,
          [
            f("quantity", "Quantity", undefined, "number"),
            f("reason", "Reason"),
            { ...f("ward", "Ward"), required: false },
          ],
        ),
      );
    if (ctx.active === "dispenses" && ctx.has("pharmacy.manage"))
      add("Return", () =>
        ctx.act(
          "Return medication",
          "pharmacy/dispenses/" + numeric(row, "id") + "/return",
          [
            f("quantity", "Returned quantity", undefined, "number"),
            f("reason", "Reason"),
          ],
        ),
      );
    if (ctx.active === "wardstock" && ctx.has("nursing.write"))
      add("Record patient use", () =>
        ctx.act(
          "Capture ward consumable",
          "pharmacy/ward-stock/" + numeric(row, "batch_id") + "/consume",
          [
            f("admissionId", "Admission", "admissions"),
            f("patientId", "Patient", "patients"),
            f("quantity", "Units used", undefined, "number"),
            f("reason", "Clinical reason"),
          ],
          { ward: row["ward"] },
        ),
      );
    if (
      ctx.active === "wardstock" &&
      (ctx.has("nursing.write") || ctx.has("pharmacy.manage"))
    )
      add("Return / waste / damage", () =>
        ctx.act(
          "Record ward stock adjustment",
          "pharmacy/ward-stock/" +
            numeric(row, "batch_id") +
            "/action?action=" +
            ctx.wardAction,
          [
            f("quantity", "Quantity", undefined, "number"),
            f("reason", "Reason"),
          ],
          { ward: row["ward"] },
        ),
      );
    if (
      ctx.active === "consumables" &&
      (ctx.has("nursing.write") || ctx.has("pharmacy.manage"))
    )
      add("Return unused consumable", () =>
        ctx.act(
          "Return patient consumable",
          "pharmacy/consumable-returns",
          [
            f("quantity", "Quantity returned", undefined, "number"),
            f("reason", "Return reason"),
          ],
          { movementId: numeric(row, "id") },
        ),
      );
    return actions;
  },
};
