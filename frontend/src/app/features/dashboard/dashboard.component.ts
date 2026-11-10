import { Component, OnDestroy, OnInit } from "@angular/core";
import { Router } from "@angular/router";
import { Subject, takeUntil } from "rxjs";
import { ApiClient } from "../../core/http/api-client.service";
import { DashboardStats } from "../../core/models/api.models";
import { SessionStore } from "../../core/auth/session.store";
import { visibleWorkspaces, portalUrl } from "../../core/auth/permission.utils";
import { NotificationService } from "../../core/services/notification.service";
import { WORKSPACES } from "../../core/navigation/workspaces";
import { apiErrorMessage } from "../../core/http/api-error";
@Component({
  selector: "app-dashboard",
  templateUrl: "./dashboard.component.html",
})
export class DashboardComponent implements OnInit, OnDestroy {
  stats: DashboardStats = {
    patients: 0,
    encounters: 0,
    labOrders: 0,
    invoices: 0,
  };
  error = "";
  private readonly destroyed = new Subject<void>();
  constructor(
    private readonly api: ApiClient,
    public readonly session: SessionStore,
    public readonly notices: NotificationService,
    private readonly router: Router,
  ) {}
  get menu() {
    return visibleWorkspaces(this.session.user, WORKSPACES);
  }
  ngOnInit(): void {
    if (this.session.user?.role === "DOCTOR") return;
    this.api
      .get<DashboardStats>("dashboard")
      .pipe(takeUntil(this.destroyed))
      .subscribe({
        next: (stats) => (this.stats = stats),
        error: (error) => (this.error = apiErrorMessage(error)),
      });
  }
  open(id: string): void {
    if (this.session.user)
      void this.router.navigateByUrl(portalUrl(this.session.user) + "/" + id);
  }
  ngOnDestroy(): void {
    this.destroyed.next();
    this.destroyed.complete();
  }
}
