import { Component, OnDestroy } from "@angular/core";
import { FormControl, FormGroup, Validators } from "@angular/forms";
import { ActivatedRoute, Router } from "@angular/router";
import { Subject, takeUntil } from "rxjs";
import { AuthService } from "../../core/auth/auth.service";
import { portalUrl } from "../../core/auth/permission.utils";
import { apiErrorMessage } from "../../core/http/api-error";
@Component({ selector: "app-login", templateUrl: "./login.component.html" })
export class LoginComponent implements OnDestroy {
  readonly form = new FormGroup({
    username: new FormControl("", {
      nonNullable: true,
      validators: [Validators.required],
    }),
    password: new FormControl("", {
      nonNullable: true,
      validators: [Validators.required],
    }),
  });
  busy = false;
  error = "";
  message = "";
  private readonly destroyed = new Subject<void>();
  constructor(
    private readonly auth: AuthService,
    private readonly router: Router,
    private readonly route: ActivatedRoute,
  ) {
    const state = history.state as { message?: string; username?: string };
    this.message = state?.message || "";
    if (state?.username) this.form.controls.username.setValue(state.username);
    if (route.snapshot.queryParamMap.get("reason") === "sessionExpired")
      this.error = "Your session expired. Sign in again to continue.";
  }
  submit(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid || this.busy) return;
    this.busy = true;
    this.error = "";
    const request = this.form.getRawValue();
    this.auth
      .login(request.username, request.password)
      .pipe(takeUntil(this.destroyed))
      .subscribe({
        next: (user) => {
          this.busy = false;
          this.form.controls.password.reset();
          const base = portalUrl(user),
            returnUrl = this.route.snapshot.queryParamMap.get("returnUrl");
          void this.router.navigateByUrl(
            returnUrl &&
              (returnUrl === base || returnUrl.startsWith(base + "/"))
              ? returnUrl
              : base,
          );
        },
        error: (error) => {
          this.busy = false;
          this.error = apiErrorMessage(error);
        },
      });
  }
  ngOnDestroy(): void {
    this.destroyed.next();
    this.destroyed.complete();
  }
}
