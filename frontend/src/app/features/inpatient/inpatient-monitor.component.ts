import { Component, Input, OnDestroy, OnInit, DoCheck } from "@angular/core";
import { Router } from "@angular/router";
import {
  Subject,
  Subscription,
  forkJoin,
  of,
  catchError,
  takeUntil,
} from "rxjs";
import { ApiClient } from "../../core/http/api-client.service";
import { SessionStore } from "../../core/auth/session.store";
import { portalUrl } from "../../core/auth/permission.utils";
import { JsonRecord } from "../../core/models/api.models";
import { WorkspaceFacade } from "../../shared/workspace/workspace.facade";
import { text, numeric } from "../../shared/utils/record.utils";
import {
  hospitalTime,
  hospitalDay,
  elapsedMinutes,
  patientLabel,
  initials,
} from "../../shared/clinical/care-view.utils";
@Component({
  selector: "app-inpatient-monitor",
  templateUrl: "./inpatient-monitor.component.html",
})
export class InpatientMonitorComponent implements OnInit, DoCheck, OnDestroy {
  @Input() context!: WorkspaceFacade;
  ward = "";
  status = "ADMITTED";
  mode = "cards";
  loading = false;
  warning = "";
  beds: JsonRecord[] | null = null;
  requests: JsonRecord[] | null = null;
  medications: JsonRecord[] | null = null;
  private lastRows?: JsonRecord[];
  private subscription?: Subscription;
  private readonly destroyed = new Subject<void>();
  readonly text = text;
  readonly initials = initials;
  constructor(
    private api: ApiClient,
    private session: SessionStore,
    private router: Router,
  ) {}
  ngOnInit(): void {
    this.load();
    this.lastRows = this.context.rows;
  }
  ngDoCheck(): void {
    if (this.lastRows !== this.context.rows) {
      this.lastRows = this.context.rows;
      this.load();
    }
  }
  load(): void {
    this.subscription?.unsubscribe();
    this.loading = true;
    this.warning = "";
    this.beds = null;
    this.requests = null;
    this.medications = null;
    const read = (path: string) =>
      this.api.get<JsonRecord[]>(path).pipe(
        catchError(() => {
          this.warning =
            "Some monitoring data could not be loaded. Refresh to retry.";
          return of(null);
        }),
      );
    this.subscription = forkJoin({
      beds: read("inpatient/beds"),
      requests: read("inpatient/recommendations"),
      medications: read("ward/medications"),
    })
      .pipe(takeUntil(this.destroyed))
      .subscribe((data) => {
        this.beds = data.beds;
        this.requests = data.requests;
        this.medications = data.medications;
        this.loading = false;
      });
  }
  get active(): JsonRecord[] {
    return this.context.rows.filter((r) => text(r, "status") === "ADMITTED");
  }
  get filtered(): JsonRecord[] {
    return this.context.filtered.filter(
      (r) =>
        (!this.ward || text(r, "ward") === this.ward) &&
        (!this.status || text(r, "status") === this.status),
    );
  }
  get wards(): string[] {
    return [
      ...new Set(
        [
          ...(this.beds || []).map((r) => text(r, "ward")),
          ...this.context.rows.map((r) => text(r, "ward")),
        ].filter(Boolean),
      ),
    ].sort();
  }
  get pending(): number | null {
    return this.requests === null
      ? null
      : this.requests.filter((r) => text(r, "status") === "PENDING").length;
  }
  get due(): number | null {
    return this.medications === null
      ? null
      : this.medications.filter(
          (r) =>
            text(r, "status") === "ORDERED" &&
            elapsedMinutes(r["scheduled_at"]) !== null &&
            this.isDue(r),
        ).length;
  }
  isDue(row: JsonRecord): boolean {
    const value = String(row["scheduled_at"] || "");
    return !!value && hospitalTime(value) <= Date.now();
  }
  countBed(state: string): number | null {
    return this.beds === null
      ? null
      : this.beds.filter(
          (r) =>
            (!this.ward || text(r, "ward") === this.ward) &&
            text(r, "status") === state,
        ).length;
  }
  wardBeds(ward: string): JsonRecord[] {
    return (this.beds || []).filter((r) => text(r, "ward") === ward);
  }
  wardOccupied(ward: string): number {
    return this.wardBeds(ward).filter((r) => text(r, "status") === "OCCUPIED")
      .length;
  }
  percent(ward: string): number {
    const total = this.wardBeds(ward).length;
    return total ? Math.round((this.wardOccupied(ward) * 100) / total) : 0;
  }
  name(row: JsonRecord): string {
    return patientLabel(row, this.context.options);
  }
  stay(row: JsonRecord): string {
    const minutes = elapsedMinutes(row["admittedAt"]);
    return minutes === null
      ? "Not recorded"
      : Math.floor(minutes / 1440) + " days";
  }
  dueFor(row: JsonRecord): number | null {
    return this.medications === null
      ? null
      : this.medications.filter(
          (m) =>
            numeric(m, "admission_id") === numeric(row, "id") &&
            text(m, "status") === "ORDERED" &&
            this.isDue(m),
        ).length;
  }
  get dischargedToday(): number {
    return this.context.rows.filter(
      (r) =>
        text(r, "status") === "DISCHARGED" &&
        hospitalDay(r["dischargedAt"]) === hospitalDay(),
    ).length;
  }
  open(id: string): void {
    if (this.session.user)
      void this.router.navigateByUrl(portalUrl(this.session.user) + "/" + id);
  }
  ngOnDestroy(): void {
    this.destroyed.next();
    this.destroyed.complete();
    this.subscription?.unsubscribe();
  }
}
