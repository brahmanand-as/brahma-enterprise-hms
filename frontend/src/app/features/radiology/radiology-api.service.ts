import { Injectable } from "@angular/core";
import { Observable, map } from "rxjs";
import { ApiClient } from "../../core/http/api-client.service";
import { JsonRecord } from "../../core/models/api.models";
import {
  WorkspaceDataSource,
  WorkspaceDefinition,
} from "../../shared/workspace/workspace.models";
import { RadiologyOrder } from "./radiology.models";
@Injectable()
export class RadiologyApiService implements WorkspaceDataSource {
  constructor(private readonly api: ApiClient) {}
  load(workspace: WorkspaceDefinition): Observable<JsonRecord[]> {
    return this.api
      .get<RadiologyOrder[] | RadiologyOrder>(workspace.path!)
      .pipe(map((value) => (Array.isArray(value) ? value : [value])));
  }
}
