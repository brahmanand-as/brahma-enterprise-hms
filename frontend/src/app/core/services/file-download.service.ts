import { Injectable } from "@angular/core";
import { Observable, tap } from "rxjs";
import { ApiClient } from "../http/api-client.service";
@Injectable({ providedIn: "root" })
export class FileDownloadService {
  constructor(private readonly api: ApiClient) {}
  download(path: string, preview = false): Observable<Blob> {
    return this.api.download(path).pipe(
      tap((blob) => {
        const url = URL.createObjectURL(blob);
        if (preview) window.open(url, "_blank", "noopener");
        else {
          const anchor = document.createElement("a");
          anchor.href = url;
          anchor.download = path.includes("xlsx")
            ? "arogya-report.xlsx"
            : "arogya-document.pdf";
          anchor.click();
        }
        setTimeout(() => URL.revokeObjectURL(url), 60000);
      }),
    );
  }
}
