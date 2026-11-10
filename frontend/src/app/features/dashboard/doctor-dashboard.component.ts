import { Component, OnDestroy, OnInit } from "@angular/core";
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
import { LookupService } from "../../core/services/lookup.service";
import { JsonRecord, LookupData } from "../../core/models/api.models";
import { text } from "../../shared/utils/record.utils";
import {
  hospitalDay,
  queueRows,
  isWaiting,
  elapsedMinutes,
  patientLabel,
  initials,
  waitingAverage,
} from "../../shared/clinical/care-view.utils";
@Component({
  selector: "app-doctor-dashboard",
  templateUrl: "./doctor-dashboard.component.html",
})
export class DoctorDashboardComponent implements OnInit, OnDestroy {
  visits: JsonRecord[] = [];
  appointments: JsonRecord[] = [];
  admissions: JsonRecord[] = [];
  prescriptions: JsonRecord[] = [];
  options: LookupData = {};
  loading = false;
  warnings: string[] = [];
  unavailable: string[] = [];
  updated: Date | null = null;
  now = Date.now();
  private readonly destroyed = new Subject<void>();
  private request?: Subscription;
  private clock?: ReturnType<typeof setInterval>;
  readonly text = text;
  readonly initials = initials;
  constructor(
    public session: SessionStore,
    private api: ApiClient,
    private lookups: LookupService,
    private router: Router,
  ) {}
  ngOnInit(): void {
    this.refresh();
    this.clock = setInterval(() => (this.now = Date.now()), 30000);
  }
  refresh(): void {
    this.request?.unsubscribe();
    this.loading = true;
    this.warnings = [];
    this.unavailable = [];
    this.visits = [];
    this.appointments = [];
    this.admissions = [];
    this.prescriptions = [];
    this.updated = null;
    const read = (path: string, permission: string, title: string) =>
      this.session.has(permission)
        ? this.api.get<JsonRecord[]>(path).pipe(
            catchError(() => {
              this.warnings.push(title + " unavailable");
              this.unavailable.push(title);
              return of([] as JsonRecord[]);
            }),
          )
        : of([] as JsonRecord[]);
    this.request = forkJoin({
      visits: read("encounters", "clinical.read", "Clinical queue"),
      appointments: read("appointments", "appointments.read", "Appointments"),
      admissions: read("admissions", "ipd.read", "Inpatients"),
      prescriptions: read(
        "orders/prescriptions",
        "prescription.read",
        "Prescriptions",
      ),
      options: this.lookups.base().pipe(
        catchError(() => {
          this.warnings.push("Patient names unavailable");
          return of({} as LookupData);
        }),
      ),
    })
      .pipe(takeUntil(this.destroyed))
      .subscribe((data) => {
        this.visits = data.visits;
        this.appointments = data.appointments;
        this.admissions = data.admissions;
        this.prescriptions = data.prescriptions;
        this.options = data.options;
        this.loading = false;
        this.updated = new Date();
        this.now = Date.now();
      });
  }
  metric(title: string, value: number): number | string {
    return this.loading || this.unavailable.includes(title) ? "—" : value;
  }
  get today(): string {
    return hospitalDay();
  }
  get todaysVisits(): JsonRecord[] {
    return queueRows(this.visits, this.today, "");
  }
  get waiting(): JsonRecord[] {
    return this.todaysVisits.filter(isWaiting);
  }
  get consulting(): JsonRecord[] {
    return this.todaysVisits.filter(
      (r) => text(r, "status") === "IN_CONSULTATION",
    );
  }
  get completed(): number {
    return this.todaysVisits.filter((r) => text(r, "status") === "COMPLETED")
      .length;
  }
  get average(): number | null {
    return waitingAverage(this.todaysVisits, this.now);
  }
  get inpatients(): JsonRecord[] {
    return this.admissions.filter((r) => text(r, "status") === "ADMITTED");
  }
  get drafts(): number {
    return this.prescriptions.filter((r) => text(r, "status") === "DRAFT")
      .length;
  }
  get schedule(): JsonRecord[] {
    return this.appointments
      .filter((r) => hospitalDay(r["appointmentAt"]) === this.today)
      .sort((a, b) =>
        text(a, "appointmentAt").localeCompare(text(b, "appointmentAt")),
      );
  }
  get flow(): { label: string; count: number; color: string }[] {
    return [
      { label: "Waiting", count: this.waiting.length, color: "amber" },
      { label: "Consulting", count: this.consulting.length, color: "blue" },
      {
        label: "Billing",
        count: this.todaysVisits.filter((r) => text(r, "status") === "BILLING")
          .length,
        color: "teal",
      },
      { label: "Completed", count: this.completed, color: "green" },
    ];
  }
  name(row: JsonRecord): string {
    return patientLabel(row, this.options);
  }
  wait(row: JsonRecord): number | null {
    return elapsedMinutes(row["startedAt"], this.now);
  }
  time(row: JsonRecord): string {
    return text(row, "appointmentAt").slice(11, 16);
  }
  open(id: string): void {
    if (this.session.user)
      void this.router.navigateByUrl(portalUrl(this.session.user) + "/" + id);
  }
  ngOnDestroy(): void {
    this.destroyed.next();
    this.destroyed.complete();
    if (this.clock) clearInterval(this.clock);
  }
}
