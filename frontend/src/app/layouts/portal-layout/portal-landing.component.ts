import { Component, OnInit } from "@angular/core";
import { Router } from "@angular/router";
import { SessionStore } from "../../core/auth/session.store";
import { portalUrl } from "../../core/auth/permission.utils";
@Component({ selector: "app-portal-landing", template: "" })
export class PortalLandingComponent implements OnInit {
  constructor(
    private readonly session: SessionStore,
    private readonly router: Router,
  ) {}
  ngOnInit(): void {
    const user = this.session.user;
    if (user)
      void this.router.navigateByUrl(
        portalUrl(user) +
          (user.role === "PATIENT" ? "/myappointments" : "/overview"),
        { replaceUrl: true },
      );
  }
}
