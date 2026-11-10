import { Component, Input, OnDestroy, OnInit } from "@angular/core";
import { JsonRecord } from "../../core/models/api.models";
import { WorkspaceFacade } from "../../shared/workspace/workspace.facade";
import { text } from "../../shared/utils/record.utils";
import {
  hospitalDay,
  elapsedMinutes,
  patientLabel,
  initials,
  queueRows,
  waitingAverage,
  isWaiting,
} from "../../shared/clinical/care-view.utils";
@Component({
  selector: "app-opd-board",
  templateUrl: "./opd-board.component.html",
})
export class OpdBoardComponent implements OnInit, OnDestroy {
  @Input() context!: WorkspaceFacade;
  day = hospitalDay();
  doctor = "";
  mode = "board";
  now = Date.now();
  readonly initials = initials;
  readonly text = text;
  readonly lanes = [
    {
      id: "WAITING",
      label: "Waiting for doctor",
      hint: "Checked in · ready for care",
      color: "amber",
    },
    {
      id: "IN_CONSULTATION",
      label: "In consultation",
      hint: "With the clinical team",
      color: "blue",
    },
    {
      id: "BILLING",
      label: "Billing & completion",
      hint: "Consultation signed",
      color: "teal",
    },
    {
      id: "COMPLETED",
      label: "Completed",
      hint: "Visit closed",
      color: "green",
    },
  ];
  private clock?: ReturnType<typeof setInterval>;
  ngOnInit(): void {
    this.clock = setInterval(() => (this.now = Date.now()), 30000);
  }
  ngOnDestroy(): void {
    if (this.clock) clearInterval(this.clock);
  }
  get rows(): JsonRecord[] {
    return queueRows(this.context.filtered, this.day, this.doctor);
  }
  get doctors(): string[] {
    return [
      ...new Set(
        this.context.rows.map((r) => text(r, "doctorName")).filter(Boolean),
      ),
    ].sort();
  }
  lane(id: string): JsonRecord[] {
    return this.rows.filter((r) =>
      id === "WAITING" ? isWaiting(r) : text(r, "status") === id,
    );
  }
  get average(): number | null {
    return waitingAverage(this.rows, this.now);
  }
  get longest(): number | null {
    const values = this.lane("WAITING")
      .map((r) => elapsedMinutes(r["startedAt"], this.now))
      .filter((v): v is number => v !== null);
    return values.length ? Math.max(...values) : null;
  }
  get other(): JsonRecord[] {
    return this.rows.filter(
      (r) =>
        !isWaiting(r) &&
        !["IN_CONSULTATION", "BILLING", "COMPLETED"].includes(
          text(r, "status"),
        ),
    );
  }
  name(row: JsonRecord): string {
    return patientLabel(row, this.context.options);
  }
  minutes(row: JsonRecord): number | null {
    return elapsedMinutes(row["startedAt"], this.now);
  }
  time(row: JsonRecord): string {
    const value = row["startedAt"];
    return typeof value === "string" ? value.slice(11, 16) : "—";
  }
  reset(): void {
    this.day = "";
    this.doctor = "";
  }
}
