import { Component, Input } from "@angular/core";
import { JsonRecord } from "../../core/models/api.models";
import { WorkspaceFacade } from "../../shared/workspace/workspace.facade";
import { field, numeric } from "../../shared/utils/record.utils";
import { Bed } from "./inpatient.models";
@Component({
  selector: "app-bed-board",
  templateUrl: "./bed-board.component.html",
})
export class BedBoardComponent {
  @Input() context!: WorkspaceFacade;
  bedWard = "";
  bedFloor = "";
  bedType = "";
  bedStatus = "";
  get rows(): Bed[] {
    return this.context.rows;
  }
  get bedRows(): Bed[] {
    return (this.context.filtered as Bed[]).filter(
      (bed) =>
        (!this.bedWard || bed.ward === this.bedWard) &&
        (!this.bedFloor || bed.floor_name === this.bedFloor) &&
        (!this.bedType || bed.bed_type === this.bedType) &&
        (!this.bedStatus || bed.status === this.bedStatus),
    );
  }
  get wards() {
    return [...new Set(this.rows.map((bed) => bed.ward))];
  }
  get floors() {
    return [...new Set(this.rows.map((bed) => bed.floor_name))];
  }
  get bedTypes() {
    return [...new Set(this.rows.map((bed) => bed.bed_type))];
  }
  bedCount(status: string): number {
    return this.rows.filter((bed) => bed.status === status).length;
  }
  label(status: string | undefined): string {
    return this.context.label(status || "");
  }
  edit(bed: JsonRecord): void {
    this.context.act(
      "Update bed location / rates",
      "inpatient/beds/" + numeric(bed, "id"),
      [
        field("ward", "Ward"),
        field("bedNo", "Bed number"),
        field("floorName", "Floor"),
        field("bedType", "Bed type"),
        field("dailyRate", "Daily rate", undefined, "number"),
      ],
      {
        ward: bed["ward"],
        bedNo: bed["bed_no"],
        floorName: bed["floor_name"],
        bedType: bed["bed_type"],
        dailyRate: bed["daily_rate"],
      },
      bed,
      "PUT",
    );
  }
  manage(bed: JsonRecord): void {
    this.context.act(
      "Change bed availability",
      "inpatient/beds/" + numeric(bed, "id") + "/status",
      [
        field("status", "Status", undefined, "select", [
          "AVAILABLE",
          "RESERVED",
          "CLEANING",
          "MAINTENANCE",
          "BLOCKED",
        ]),
        {
          ...field("patientId", "Reservation patient", "patients"),
          required: false,
        },
        field("reason", "Reason"),
      ],
    );
  }
}
