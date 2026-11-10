import { InjectionToken, Type } from "@angular/core";
import { Observable } from "rxjs";
import { JsonRecord, JsonValue } from "../../core/models/api.models";
export type FieldType =
  | "text"
  | "number"
  | "password"
  | "date"
  | "month"
  | "time"
  | "datetime-local"
  | "textarea"
  | "select"
  | "checkbox";
export type FormValue = string | number | boolean | null;
export interface Field {
  key: string;
  label: string;
  type?: FieldType;
  source?: string;
  values?: string[];
  required?: boolean;
}
export interface WorkspaceDefinition {
  id: string;
  label: string;
  icon: string;
  path?: string;
  permission?: string;
  columns?: string[];
  patient?: boolean;
}
export type MutationMethod = "POST" | "PUT" | "PATCH";
export interface Task {
  label: string;
  path: string;
  permission?: string;
  fields: Field[];
  lines?: Field[];
  lineKey?: string;
  method?: MutationMethod;
  defaults?: JsonRecord;
  initialPermissions?: string[];
}
export interface RowAction {
  label: string;
  run: () => void;
}
export interface WorkflowContext {
  active: string;
  role: string;
  username: string;
  employeeId: number | null;
  batchAction: string;
  wardAction: string;
  selected: JsonRecord | null;
  detail: JsonValue | null;
  has(permission?: string): boolean;
  label(key: string): string;
  act(
    label: string,
    path: string,
    fields?: Field[],
    defaults?: JsonRecord,
    row?: JsonRecord | null,
    method?: MutationMethod,
    lines?: Field[],
    lineKey?: string,
  ): void;
  execute(label: string, path: string, body?: JsonRecord): void;
  showDetail(path: string): void;
  download(path: string, preview?: boolean): void;
}
export interface WorkflowDefinition {
  tasks(context: WorkflowContext): Task[];
  actions(context: WorkflowContext, row: JsonRecord): RowAction[];
}
export interface WorkspaceDataSource {
  load(workspace: WorkspaceDefinition): Observable<JsonRecord[]>;
  prepare?(task: Task, payload: JsonRecord): Observable<JsonRecord>;
}
export const WORKSPACE_WORKFLOWS = new InjectionToken<WorkflowDefinition>(
  "WORKSPACE_WORKFLOWS",
);
export const WORKSPACE_DATA_SOURCE = new InjectionToken<WorkspaceDataSource>(
  "WORKSPACE_DATA_SOURCE",
);

export const WORKSPACE_VIEWS = new InjectionToken<
  Record<string, Type<unknown>>
>("WORKSPACE_VIEWS");
