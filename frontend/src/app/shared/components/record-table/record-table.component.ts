import { Component, EventEmitter, Input, Output } from "@angular/core";
import { JsonRecord, LookupData } from "../../../core/models/api.models";
import { RowAction } from "../../workspace/workspace.models";
import { displayValue, label } from "../../utils/record.utils";
@Component({
  selector: "app-record-table",
  templateUrl: "./record-table.component.html",
})
export class RecordTableComponent {
  @Input() rows: JsonRecord[] = [];
  @Input() columns: string[] = [];
  @Input() options: LookupData = {};
  @Input() busy = false;
  @Input() actions: (row: JsonRecord) => RowAction[] = () => [];
  readonly label = label;
  value(row: JsonRecord, key: string): string {
    return displayValue(row, key, this.options);
  }
  trackRow(index: number, row: JsonRecord): string | number {
    return typeof row["id"] === "number" ? row["id"] : index;
  }
}
