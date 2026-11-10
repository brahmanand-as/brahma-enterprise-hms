import { Component } from "@angular/core";
import { Router } from "@angular/router";
import { AuthService } from "../../core/auth/auth.service";
import { SessionStore } from "../../core/auth/session.store";
import { visibleWorkspaces, portalUrl } from "../../core/auth/permission.utils";
import { WORKSPACES } from "../../core/navigation/workspaces";
import { NotificationService } from "../../core/services/notification.service";
import { label } from "../../shared/utils/record.utils";
@Component({
  selector: "app-portal-layout",
  templateUrl: "./portal-layout.component.html",
})
export class PortalLayoutComponent {
  readonly label = label;
  constructor(
    public readonly session: SessionStore,
    public readonly auth: AuthService,
    public readonly notices: NotificationService,
    private readonly router: Router,
  ) {}
  get menu() {
    return visibleWorkspaces(this.session.user, WORKSPACES);
  }
  get active(): string {
    return (
      this.router.url.split("?")[0].split("/")[2] ||
      (this.session.user?.role === "PATIENT" ? "myappointments" : "overview")
    );
  }
  get current() {
    return (
      WORKSPACES.find((workspace) => workspace.id === this.active) ||
      WORKSPACES[0]
    );
  }
  navigate(id: string): void {
    if (this.session.user)
      void this.router.navigateByUrl(portalUrl(this.session.user) + "/" + id);
  }
}
