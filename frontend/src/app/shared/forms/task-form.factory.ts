import {
  AbstractControl,
  FormArray,
  FormControl,
  FormGroup,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from "@angular/forms";
import { JsonRecord } from "../../core/models/api.models";
import { Field, FormValue, Task } from "../workspace/workspace.models";
export type FieldGroup = FormGroup<Record<string, FormControl<FormValue>>>;
const nonBlank: ValidatorFn = (
  control: AbstractControl,
): ValidationErrors | null =>
  typeof control.value === "string" && !control.value.trim()
    ? { required: true }
    : null;
export function validatorsFor(field: Field): ValidatorFn[] {
  const rules: ValidatorFn[] = [];
  if (field.required !== false && field.type !== "checkbox")
    rules.push(Validators.required, nonBlank);
  if (field.type === "number") rules.push(Validators.min(0));
  if (field.key === "mobile") rules.push(Validators.pattern(/^[0-9]{10}$/));
  if (field.key === "username")
    rules.push(Validators.pattern(/^[A-Za-z0-9._-]{3,80}$/));
  if (field.type === "password") rules.push(Validators.minLength(12));
  if (field.key === "spo2") rules.push(Validators.max(100));
  return rules;
}
export function fieldGroup(
  fields: Field[],
  defaults: JsonRecord = {},
): FieldGroup {
  const controls: Record<string, FormControl<FormValue>> = {};
  for (const field of fields) {
    const value = defaults[field.key];
    controls[field.key] = new FormControl<FormValue>(
      typeof value === "string" ||
      typeof value === "number" ||
      typeof value === "boolean"
        ? value
        : field.type === "checkbox" && field.required !== false
          ? false
          : null,
      validatorsFor(field),
    );
  }
  return new FormGroup(controls);
}
export const verifiedPayment: ValidatorFn = (
  control: AbstractControl,
): ValidationErrors | null => {
  const method = control.get("method")?.value;
  if (!method || method === "CASH") return null;
  return control.get("referenceNo")?.value &&
    control.get("verified")?.value === true
    ? null
    : { verifiedPayment: true };
};
export const dispensingSource: ValidatorFn = (
  control: AbstractControl,
): ValidationErrors | null => {
  const fields = control.get("fields");
  const prescription = fields?.get("prescriptionId")?.value,
    order = fields?.get("medicationOrderId")?.value;
  return Number(!!prescription) + Number(!!order) === 1
    ? null
    : { dispensingSource: true };
};
export function createTaskForm(task: Task) {
  const lines = new FormArray<FieldGroup>([]);
  if (task.lines) {
    const group = fieldGroup(task.lines);
    if (task.lineKey === "parts") group.addValidators(verifiedPayment);
    lines.push(group);
  }
  return new FormGroup(
    {
      fields: fieldGroup(task.fields, task.defaults),
      lines,
      permissions: new FormControl<string[]>(task.initialPermissions || [], {
        nonNullable: true,
      }),
    },
    task.path === "pharmacy/dispenses"
      ? { validators: dispensingSource }
      : undefined,
  );
}
export function buildPayload(
  task: Task,
  fields: Record<string, FormValue>,
  lines: Record<string, FormValue>[],
  permissions: string[],
  roleEditor: boolean,
): JsonRecord {
  const payload: JsonRecord = { ...(task.defaults || {}) };
  for (const field of task.fields) {
    const value = fields[field.key];
    if (field.required === false && (value === null || value === ""))
      delete payload[field.key];
    else payload[field.key] = value;
  }
  if (task.lineKey)
    payload[task.lineKey] = lines.map((line) => {
      const result: JsonRecord = {};
      for (const field of task.lines || []) {
        const value = line[field.key];
        if (value !== null && !(field.required === false && value === ""))
          result[field.key] = value;
      }
      return result;
    });
  if (roleEditor) payload["permissions"] = permissions;
  return payload;
}
