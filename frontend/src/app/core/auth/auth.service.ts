import { Injectable } from "@angular/core";
import { Router } from "@angular/router";
import { Observable, catchError, switchMap, tap, throwError } from "rxjs";
import { ApiClient } from "../http/api-client.service";
import {
  CurrentUser,
  LoginResponse,
  RegistrationRequest,
  RegistrationResponse,
} from "../models/api.models";
import { SessionStore } from "./session.store";
@Injectable({ providedIn: "root" })
export class AuthService {
  constructor(
    private readonly api: ApiClient,
    public readonly session: SessionStore,
    private readonly router: Router,
  ) {
    session.expired$.subscribe(
      () =>
        void router.navigate(["/login"], {
          queryParams: { reason: "sessionExpired" },
        }),
    );
  }
  login(username: string, password: string): Observable<CurrentUser> {
    this.session.clear();
    return this.api
      .post<LoginResponse>("auth/login", { username, password })
      .pipe(
        switchMap((response) => {
          this.session.setToken(response.token);
          return this.api
            .get<CurrentUser>("auth/me")
            .pipe(
              tap((user) =>
                this.session.establish(
                  {
                    ...user,
                    permissions: user.permissions || [],
                    employeeId: user.employeeId ?? null,
                  },
                  response.expiresIn,
                ),
              ),
            );
        }),
        catchError((error: unknown) => {
          this.session.clear();
          return throwError(() => error);
        }),
      );
  }
  register(request: RegistrationRequest): Observable<RegistrationResponse> {
    return this.api.post<RegistrationResponse>("public/register", request);
  }
  logout(): void {
    // Subscribe before clearing the token so the interceptor can attach it to revocation.
    this.api.post("auth/logout", {}).subscribe({ error: () => undefined });
    this.session.clear();
    void this.router.navigate(["/login"]);
  }
}
