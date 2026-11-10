import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { JsonRecord } from "../models/api.models";
import { MutationMethod } from "../../shared/workspace/workspace.models";
@Injectable({ providedIn: "root" })
export class ApiClient {
  constructor(private readonly http: HttpClient) {}
  get<T>(path: string): Observable<T> {
    return this.http.get<T>(this.url(path));
  }
  post<T>(path: string, body: object): Observable<T> {
    return this.http.post<T>(this.url(path), body);
  }
  mutate<T>(
    method: MutationMethod,
    path: string,
    body: JsonRecord,
  ): Observable<T> {
    return this.http.request<T>(method, this.url(path), { body });
  }
  download(path: string): Observable<Blob> {
    return this.http.get(this.url(path), { responseType: "blob" });
  }
  private url(path: string): string {
    return "/api/" + path.replace(/^\/+/, "");
  }
}
