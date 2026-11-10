import { Injectable } from "@angular/core";
import {
  ActivatedRouteSnapshot,
  CanActivate,
  Router,
  UrlTree,
} from "@angular/router";
import { SessionStore } from "./session.store";
import { visibleWorkspaces, portalUrl } from "./permission.utils";
import { WORKSPACES } from "../navigation/workspaces";
@Injectable({ providedIn: "root" })
export class WorkspaceGuard implements CanActivate {
  constructor(
    private readonly session: SessionStore,
    private readonly router: Router,
  ) {}
  canActivate(route: ActivatedRouteSnapshot): boolean | UrlTree {
    const user = this.session.user;
    if (!user) return this.router.createUrlTree(["/login"]);
    return visibleWorkspaces(user, WORKSPACES).some(
      (workspace) => workspace.id === route.data["workspaceId"],
    )
      ? true
      : this.router.parseUrl(portalUrl(user));
  }
}
