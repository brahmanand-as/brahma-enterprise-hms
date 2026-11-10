import { Injectable } from "@angular/core";
import {
  HttpErrorResponse,
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
} from "@angular/common/http";
import { Observable, catchError, throwError } from "rxjs";
import { SessionStore } from "../auth/session.store";
@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private readonly session: SessionStore) {}
  intercept(
    request: HttpRequest<unknown>,
    next: HttpHandler,
  ): Observable<HttpEvent<unknown>> {
    const token = this.session.token;
    const protectedRequest =
      request.url.startsWith("/api/") &&
      !request.url.startsWith("/api/public/") &&
      !request.url.endsWith("/auth/login");
    const outgoing =
      protectedRequest && token
        ? request.clone({ setHeaders: { Authorization: "Bearer " + token } })
        : request;
    return next.handle(outgoing).pipe(
      catchError((error: unknown) => {
        if (
          error instanceof HttpErrorResponse &&
          error.status === 401 &&
          protectedRequest &&
          token &&
          this.session.token === token
        )
          this.session.expire();
        return throwError(() => error);
      }),
    );
  }
}
