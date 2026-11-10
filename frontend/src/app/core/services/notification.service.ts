import { Injectable } from "@angular/core";
import { BehaviorSubject, of, switchMap, catchError } from "rxjs";
import { ApiClient } from "../http/api-client.service";
import { Notification } from "../models/api.models";
import { SessionStore } from "../auth/session.store";
@Injectable({ providedIn: "root" })
export class NotificationService {
  readonly notifications = new BehaviorSubject<Notification[]>([]);
  get unread(): number {
    return this.notifications.value.filter(
      (notification) => !notification.is_read,
    ).length;
  }
  constructor(
    private readonly api: ApiClient,
    private readonly session: SessionStore,
  ) {
    session.user$
      .pipe(
        switchMap((user) =>
          user
            ? this.api
                .get<Notification[]>("notifications")
                .pipe(catchError(() => of([] as Notification[])))
            : of([] as Notification[]),
        ),
      )
      .subscribe((rows) => this.notifications.next(rows));
  }
  refresh(): void {
    const token = this.session.token;
    if (!token) return;
    this.api.get<Notification[]>("notifications").subscribe({
      next: (rows) => {
        if (token === this.session.token) this.notifications.next(rows);
      },
      error: () => undefined,
    });
  }
}
