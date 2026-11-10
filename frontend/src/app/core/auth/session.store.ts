import { Injectable } from "@angular/core";
import { BehaviorSubject, Subject } from "rxjs";
import { CurrentUser } from "../models/api.models";
import { hasPermission } from "./permission.utils";
@Injectable({ providedIn: "root" })
export class SessionStore {
  private readonly state = new BehaviorSubject<CurrentUser | null>(null);
  readonly user$ = this.state.asObservable();
  readonly expired$ = new Subject<void>();
  private expiration?: ReturnType<typeof setTimeout>;
  token = "";
  get user(): CurrentUser | null {
    return this.state.value;
  }
  get authenticated(): boolean {
    return !!this.user && !!this.token;
  }
  setToken(token: string): void {
    this.token = token;
  }
  establish(user: CurrentUser, seconds: number): void {
    if (this.expiration) clearTimeout(this.expiration);
    this.state.next(user);
    this.expiration = setTimeout(
      () => this.expire(),
      Math.max(0, seconds) * 1000,
    );
  }
  clear(): void {
    if (this.expiration) clearTimeout(this.expiration);
    this.token = "";
    this.state.next(null);
  }
  expire(): void {
    if (!this.token) return;
    this.clear();
    this.expired$.next();
  }
  has(permission?: string): boolean {
    return hasPermission(this.user, permission);
  }
}
