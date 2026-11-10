import {
  WorkflowDefinition,
  Task,
  RowAction,
} from "../../shared/workspace/workspace.models";
import { field, text, numeric, flag } from "../../shared/utils/record.utils";
export const hrWorkflows: WorkflowDefinition = {
  tasks(ctx) {
    const f = field,
      t: Task[] = [];
    if (ctx.active === "heads")
      t.push({
        label: "Assign department head",
        path: "staffing/heads",
        fields: [
          f("department", "Department", "departmentNames"),
          f("username", "Approver username"),
        ],
      });
    if (ctx.active === "profiles")
      t.push({
        label: "Link doctor profile",
        path: "doctor-profiles",
        fields: [
          f("employeeId", "Doctor employee", "employees"),
          f("specialty", "Specialty"),
          f("qualification", "Qualification"),
          f("experienceYears", "Years of experience", undefined, "number"),
          f("registrationNumber", "Medical registration"),
          f("consultationFee", "Consultation fee", undefined, "number"),
          {
            ...f("biography", "Biography", undefined, "textarea"),
            required: false,
          },
        ],
      });
    if (ctx.active === "roster" && ctx.has("roster.manage"))
      t.push({
        label: "Schedule duty",
        path: "staffing/roster",
        fields: [
          f("employeeId", "Employee", "employees"),
          f("shiftDate", "Start date", undefined, "date"),
          f("startsAt", "Start time", undefined, "time"),
          f("endsOn", "End date", undefined, "date"),
          f("endsAt", "End time", undefined, "time"),
        ],
      });
    if (ctx.active === "attendance" && ctx.has("hr.manage"))
      t.push({
        label: "Record attendance",
        path: "staffing/attendance",
        fields: [
          f("employeeId", "Employee", "employees"),
          f("workDate", "Date", undefined, "date"),
          f("status", "Attendance", undefined, "select", [
            "PRESENT",
            "ABSENT",
            "LEAVE",
            "HALF_DAY",
          ]),
          f("hoursWorked", "Hours", undefined, "number"),
        ],
      });
    if (ctx.active === "employees" && ctx.has("staff.manage"))
      t.push({
        label: "Create employee",
        path: "employees",
        defaults: { status: "ACTIVE" },
        fields: [
          f("name", "Full name"),
          f("department", "Department", "departmentNames"),
          f("designation", "Designation"),
          f("mobile", "Mobile"),
        ],
      });
    if (ctx.active === "staff" && ctx.has("staff.manage"))
      t.push({
        label: "Create staff login",
        path: "staff",
        fields: [
          f("username", "Username"),
          f("password", "Temporary password", undefined, "password"),
          f("role", "Role", "roles"),
          f("employeeId", "Employee", "employees"),
        ],
      });
    if (ctx.active === "myhr" && ctx.employeeId)
      t.push({
        label: "Apply for leave",
        path: "hr/me/leaves",
        fields: [
          f("leaveType", "Leave type", undefined, "select", [
            "CASUAL",
            "SICK",
            "EARNED",
            "UNPAID",
          ]),
          f("startDate", "From", undefined, "date"),
          f("endDate", "To", undefined, "date"),
          f("reason", "Reason"),
        ],
      });
    if (ctx.active === "leave" && ctx.has("hr.manage"))
      t.push({
        label: "Publish payslip",
        path: "hr/admin/salary",
        fields: [
          f("employeeId", "Employee", "employees"),
          f("salaryMonth", "Pay month", undefined, "month"),
          f("basic", "Basic", undefined, "number"),
          f("allowances", "Allowances", undefined, "number"),
          f("deductions", "Deductions", undefined, "number"),
        ],
      });
    return t;
  },
  actions(ctx, row) {
    const f = field,
      actions: RowAction[] = [];
    const add = (label: string, run: () => void) =>
      actions.push({ label, run });
    if (ctx.active === "roster") {
      if (ctx.has("roster.manage") && text(row, "status") === "SCHEDULED")
        add("Mark absent", () =>
          ctx.execute(
            "Absence recorded",
            "enterprise/roster/" + numeric(row, "id") + "/absent",
          ),
        );
      if (
        ctx.has("roster.manage") ||
        numeric(row, "employee_id") === ctx.employeeId
      )
        add("Request replacement", () =>
          ctx.act(
            "Request replacement / swap",
            "staffing/requests",
            [
              f("replacementEmployeeId", "Replacement employee", "employees"),
              f("reason", "Reason"),
            ],
            { rosterId: numeric(row, "id") },
          ),
        );
    }
    if (
      ctx.active === "swaps" &&
      text(row, "status") === "PENDING" &&
      ctx.has("roster.approve")
    )
      ["APPROVED", "REJECTED"].forEach((status) =>
        add(ctx.label(status), () =>
          ctx.execute(
            "Request reviewed",
            "staffing/requests/" + numeric(row, "id") + "/review",
            { status },
          ),
        ),
      );
    if (ctx.active === "leave" && text(row, "status") === "PENDING")
      ["APPROVED", "REJECTED"].forEach((status) =>
        add(ctx.label(status), () =>
          ctx.execute(
            "Leave reviewed",
            "hr/admin/leaves/" + numeric(row, "id") + "/review",
            { status },
          ),
        ),
      );
    if (
      ctx.active === "myhr" &&
      text(row, "payment_status") === "PENDING" &&
      ctx.has("hr.manage")
    )
      add("Reconcile bank payment", () =>
        ctx.act(
          "Reconcile payroll",
          "staffing/salary/" + numeric(row, "id") + "/reconcile",
          [f("referenceNo", "Verified bank reference")],
        ),
      );
    if (ctx.active === "employees" && ctx.has("staff.manage"))
      add("Edit / deactivate", () =>
        ctx.act(
          "Update employee",
          "employees/" + numeric(row, "id"),
          [
            f("name", "Full name"),
            f("department", "Department", "departmentNames"),
            f("designation", "Designation"),
            f("mobile", "Mobile"),
            f("status", "Status", undefined, "select", ["ACTIVE", "INACTIVE"]),
          ],
          row,
          row,
          "PUT",
        ),
      );
    if (ctx.active === "payroll" && text(row, "payment_status") === "PENDING")
      add("Reconcile bank payment", () =>
        ctx.act(
          "Reconcile payroll payment",
          "staffing/salary/" + numeric(row, "id") + "/reconcile",
          [f("referenceNo", "Verified bank reference")],
        ),
      );
    return actions;
  },
};
