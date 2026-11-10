import { Component, EventEmitter, Input, Output } from "@angular/core";
import { JsonRecord, JsonValue } from "../../../core/models/api.models";
import { label } from "../../utils/record.utils";
@Component({
  selector: "app-record-details",
  templateUrl: "./record-details.component.html",
})
export class RecordDetailsComponent {
  @Input() detail: JsonValue | null = null;
  @Output() readonly close = new EventEmitter<void>();
  readonly label = label;
  keys(value: JsonValue | null): string[] {
    return value !== null && typeof value === "object" && !Array.isArray(value)
      ? Object.keys(value).filter(
          (key) => !/(token|password|secret)/i.test(key),
        )
      : [];
  }
  rows(value: JsonValue | undefined): JsonValue[] {
    return Array.isArray(value) ? value : [];
  }
  isObject(value: JsonValue | undefined): boolean {
    return value !== null && typeof value === "object" && !Array.isArray(value);
  }
  isArray(value: JsonValue | undefined): boolean {
    return Array.isArray(value);
  }
  property(value: JsonValue | null, key: string): JsonValue | null {
    return value !== null && typeof value === "object" && !Array.isArray(value)
      ? (value[key] ?? null)
      : null;
  }
  scalar(value: JsonValue | undefined): string {
    return value == null ? "—" : String(value);
  }
}
