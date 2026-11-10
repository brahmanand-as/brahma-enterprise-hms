import { Component, OnDestroy } from "@angular/core";
import { FormControl, FormGroup, Validators } from "@angular/forms";
import { Router } from "@angular/router";
import { Subject, takeUntil } from "rxjs";
import { AuthService } from "../../core/auth/auth.service";
import { apiErrorMessage } from "../../core/http/api-error";
@Component({
  selector: "app-register",
  templateUrl: "./register.component.html",
})
export class RegisterComponent implements OnDestroy {
  readonly form = new FormGroup({
    firstName: new FormControl("", {
      nonNullable: true,
      validators: [Validators.required],
    }),
    lastName: new FormControl("", { nonNullable: true }),
    mobile: new FormControl("", {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/^[0-9]{10}$/)],
    }),
    username: new FormControl("", {
      nonNullable: true,
      validators: [
        Validators.required,
        Validators.pattern(/^[A-Za-z0-9._-]{3,80}$/),
      ],
    }),
    password: new FormControl("", {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(12)],
    }),
  });
  busy = false;
  error = "";
  private readonly destroyed = new Subject<void>();
  constructor(
    private readonly auth: AuthService,
    private readonly router: Router,
  ) {}
  submit(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid || this.busy) return;
    this.busy = true;
    this.error = "";
    const value = this.form.getRawValue();
    this.auth
      .register(value)
      .pipe(takeUntil(this.destroyed))
      .subscribe({
        next: (response) =>
          void this.router.navigate(["/login"], {
            state: {
              message:
                "Your MRN is " +
                response.mrn +
                ". Sign in to book an appointment.",
              username: value.username,
            },
          }),
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
