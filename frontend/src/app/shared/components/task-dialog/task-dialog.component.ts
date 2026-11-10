import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  SimpleChanges,
} from "@angular/core";
import {
  JsonRecord,
  LookupData,
  LookupOption,
  Permission,
} from "../../../core/models/api.models";
import { Field, Task } from "../../workspace/workspace.models";
import { label } from "../../utils/record.utils";
import {
  buildPayload,
  createTaskForm,
  fieldGroup,
} from "../../forms/task-form.factory";
import { verifiedPayment } from "../../forms/task-form.factory";
@Component({
  selector: "app-task-dialog",
  templateUrl: "./task-dialog.component.html",
})
export class TaskDialogComponent implements OnChanges {
  @Input() task!: Task;
  @Input() workspaceLabel = "";
  @Input() options: LookupData = {};
  @Input() permissions: Permission[] = [];
  @Input() roleEditor = false;
  @Input() busy = false;
  @Input() loading = false;
  @Input() error = "";
  @Output() readonly save = new EventEmitter<JsonRecord>();
  @Output() readonly close = new EventEmitter<void>();
  form = createTaskForm({ label: "", path: "", fields: [] });
  ngOnChanges(changes: SimpleChanges): void {
    if (changes["task"]) this.form = createTaskForm(this.task);
  }
  choices(field: Field): LookupOption[] {
    if (field.values)
      return field.values.map((value) => ({ id: value, label: label(value) }));
    if (field.source === "departmentNames" || field.source === "doctorNames")
      return (
        this.options[
          field.source === "departmentNames" ? "departments" : "doctors"
        ] || []
      ).map((option) => ({ id: option.label, label: option.label }));
    if (field.source === "medicineNames")
      return (this.options["inventory"] || []).map((option) => ({
        id: option.name || option.label,
        label: option.label,
      }));
    return this.options[field.source || ""] || [];
  }
  addLine(): void {
    const group = fieldGroup(this.task.lines || []);
    if (this.task.lineKey === "parts") group.addValidators(verifiedPayment);
    this.form.controls.lines.push(group);
  }
  removeLine(index: number): void {
    if (this.form.controls.lines.length > 1)
      this.form.controls.lines.removeAt(index);
  }
  togglePermission(code: string): void {
    const control = this.form.controls.permissions;
    control.setValue(
      control.value.includes(code)
        ? control.value.filter((value) => value !== code)
        : [...control.value, code],
    );
  }
  submit(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid || this.busy || this.loading) return;
    this.save.emit(
      buildPayload(
        this.task,
        this.form.controls.fields.getRawValue(),
        this.form.controls.lines.getRawValue(),
        this.form.controls.permissions.value,
        this.roleEditor,
      ),
    );
  }
}
