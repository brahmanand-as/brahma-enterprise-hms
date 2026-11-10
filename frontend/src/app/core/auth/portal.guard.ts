import { Injectable } from "@angular/core";
import {
  ActivatedRouteSnapshot,
  CanActivate,
  Router,
  RouterStateSnapshot,
  UrlTree,
} from "@angular/router";
import { SessionStore } from "./session.store";
import { portalUrl } from "./permission.utils";
@Injectable({ providedIn: "root" })
export class PortalGuard implements CanActivate {
  constructor(
    private readonly session: SessionStore,
    private readonly router: Router,
  ) {}
  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot,
  ): boolean | UrlTree {
    if (!this.session.authenticated)
      return this.router.createUrlTree(["/login"], {
        queryParams: { returnUrl: state.url },
      });
    const user = this.session.user!;
    return route.paramMap.get("portal") === user.role.toLowerCase()
      ? true
      : this.router.parseUrl(portalUrl(user));
  }
}
