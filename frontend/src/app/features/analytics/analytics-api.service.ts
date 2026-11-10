import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { ApiClient } from "../../core/http/api-client.service";
import { AnalyticsReport } from "./analytics.models";
@Injectable()
export class AnalyticsApiService {
  constructor(private readonly api: ApiClient) {}
  overview(month: string, department: string): Observable<AnalyticsReport> {
    return this.api.get<AnalyticsReport>(
      "reports/overview?" + new URLSearchParams({ month, department }),
    );
  }
  exportPath(
    format: "xlsx" | "pdf",
    month: string,
    department: string,
  ): string {
    return (
      "reports/export." +
      format +
      "?" +
      new URLSearchParams({ month, department })
    );
  }
}
