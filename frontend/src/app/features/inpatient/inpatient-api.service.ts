import { Injectable } from "@angular/core";
import { Observable, map, of, throwError } from "rxjs";
import { ApiClient } from "../../core/http/api-client.service";
import { JsonRecord } from "../../core/models/api.models";
import {
  WorkspaceDataSource,
  WorkspaceDefinition,
  Task,
} from "../../shared/workspace/workspace.models";
import { Bed } from "./inpatient.models";
@Injectable()
export class InpatientApiService implements WorkspaceDataSource {
  constructor(private readonly api: ApiClient) {}
  load(workspace: WorkspaceDefinition): Observable<JsonRecord[]> {
    return this.api
      .get<Bed[] | Bed>(workspace.path!)
      .pipe(map((value) => (Array.isArray(value) ? value : [value])));
  }

  prepare(task: Task, payload: JsonRecord): Observable<JsonRecord> {
    if (task.path !== "operations/ipd/admit") return of(payload);
    return this.api.get<Bed[]>("inpatient/beds").pipe(
      map((beds) => {
        const bed = beds.find((value) => value.id === payload["bedId"]);
        if (!bed) throw new Error("Select an available bed");
        return {
          patientId: payload["patientId"],
          ward: bed.ward,
          bedNo: bed.bed_no,
        };
      }),
    );
  }
}
