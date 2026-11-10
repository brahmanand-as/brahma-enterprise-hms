import { Component, OnDestroy, OnInit } from "@angular/core";
import { FormControl, FormGroup } from "@angular/forms";
import { Subject, Subscription, takeUntil } from "rxjs";
import { AnalyticsApiService } from "./analytics-api.service";
import { AnalyticsReport } from "./analytics.models";
import { LookupService } from "../../core/services/lookup.service";
import { FileDownloadService } from "../../core/services/file-download.service";
import { LookupOption } from "../../core/models/api.models";
import { apiErrorMessage } from "../../core/http/api-error";
import { label } from "../../shared/utils/record.utils";
@Component({
  selector: "app-analytics",
  templateUrl: "./analytics.component.html",
})
export class AnalyticsComponent implements OnInit, OnDestroy {
  readonly label = label;
  readonly filters = new FormGroup({
    month: new FormControl(
      new Date().getFullYear() +
        "-" +
        String(new Date().getMonth() + 1).padStart(2, "0"),
      { nonNullable: true },
    ),
    department: new FormControl("", { nonNullable: true }),
  });
  report: AnalyticsReport | null = null;
  departments: LookupOption[] = [];
  error = "";
  private readonly destroyed = new Subject<void>();
  private request?: Subscription;
  constructor(
    private readonly api: AnalyticsApiService,
    private readonly lookups: LookupService,
    private readonly files: FileDownloadService,
  ) {}
  ngOnInit(): void {
    this.lookups
      .base()
      .pipe(takeUntil(this.destroyed))
      .subscribe({
        next: (options) => (this.departments = options["departments"] || []),
        error: (error) => (this.error = apiErrorMessage(error)),
      });
    this.apply();
  }
  apply(): void {
    this.request?.unsubscribe();
    this.error = "";
    const values = this.filters.getRawValue();
    this.request = this.api
      .overview(values.month, values.department)
      .pipe(takeUntil(this.destroyed))
      .subscribe({
        next: (report) => (this.report = report),
        error: (error) => (this.error = apiErrorMessage(error)),
      });
  }
  download(format: "xlsx" | "pdf"): void {
    const values = this.filters.getRawValue();
    this.files
      .download(this.api.exportPath(format, values.month, values.department))
      .pipe(takeUntil(this.destroyed))
      .subscribe({ error: (error) => (this.error = apiErrorMessage(error)) });
  }
  ngOnDestroy(): void {
    this.destroyed.next();
    this.destroyed.complete();
  }
}
