import { Component, Input } from "@angular/core";
import { FormControl } from "@angular/forms";
import { LookupOption } from "../../../core/models/api.models";
import { Field, FormValue } from "../../workspace/workspace.models";
@Component({
  selector: "app-form-field",
  templateUrl: "./form-field.component.html",
  styles: [
    `
      :host {
        display: block;
        min-width: 0;
      }
      :host.wide {
        grid-column: 1/-1;
      }
    `,
  ],
})
export class FormFieldComponent {
  @Input() field!: Field;
  @Input() control!: FormControl<FormValue>;
  @Input() options: LookupOption[] = [];
  get invalid(): boolean {
    return this.control.invalid && (this.control.touched || this.control.dirty);
  }
  get validationMessage(): string {
    if (this.control.hasError("required"))
      return this.field.label + " is required.";
    if (this.control.hasError("minlength"))
      return "Use at least 12 characters.";
    if (this.control.hasError("pattern"))
      return this.field.key === "mobile"
        ? "Enter a ten-digit mobile number."
        : "Use 3–80 letters, numbers, dots, underscores or hyphens.";
    if (this.control.hasError("max")) return "The maximum value is 100.";
    return "Enter a valid nonnegative value.";
  }
}
