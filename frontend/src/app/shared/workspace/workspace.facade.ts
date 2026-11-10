import { Inject, Injectable, OnDestroy } from "@angular/core";
import { Subject, Subscription, of, switchMap, takeUntil } from "rxjs";
import { ApiClient } from "../../core/http/api-client.service";
import { SessionStore } from "../../core/auth/session.store";
import { readableApiError } from "../../core/http/api-error";
import { FileDownloadService } from "../../core/services/file-download.service";
import { LookupService } from "../../core/services/lookup.service";
import { NotificationService } from "../../core/services/notification.service";
import {
  JsonRecord,
  JsonValue,
  LookupData,
  Permission,
} from "../../core/models/api.models";
import {
  Field,
  MutationMethod,
  Task,
  WorkflowContext,
  WorkspaceDataSource,
  WorkspaceDefinition,
  WorkflowDefinition,
  WORKSPACE_DATA_SOURCE,
  WORKSPACE_WORKFLOWS,
} from "./workspace.models";
import { label, stringList } from "../utils/record.utils";
@Injectable()
export class WorkspaceFacade implements WorkflowContext, OnDestroy {
  active = "";
  workspace!: WorkspaceDefinition;
  search = "";
  rows: JsonRecord[] = [];
  options: LookupData = {};
  busy = false;
  message = "";
  error = "";
  task: Task | null = null;
  taskError = "";
  optionsLoading = false;
  permissionCatalog: Permission[] = [];
  selected: JsonRecord | null = null;
  detail: JsonValue | null = null;
  batchAction = "WASTE";
  wardAction = "RETURN";
  private readonly destroyed = new Subject<void>();
  private loadSubscription?: Subscription;
  private lookupSubscription?: Subscription;
  private detailSubscription?: Subscription;
  private destroyedFlag = false;
  readonly label = label;
  constructor(
    private readonly api: ApiClient,
    private readonly session: SessionStore,
    private readonly lookups: LookupService,
    private readonly notices: NotificationService,
    private readonly files: FileDownloadService,
    @Inject(WORKSPACE_WORKFLOWS) private readonly workflows: WorkflowDefinition,
    @Inject(WORKSPACE_DATA_SOURCE) private readonly data: WorkspaceDataSource,
  ) {}
  get role(): string {
    return this.session.user?.role || "";
  }
  get username(): string {
    return this.session.user?.username || "";
  }
  get employeeId(): number | null {
    return this.session.user?.employeeId ?? null;
  }
  get tasks(): Task[] {
    return this.workflows.tasks(this);
  }
  get filtered(): JsonRecord[] {
    const query = this.search.toLowerCase();
    return this.rows.filter((row) =>
      JSON.stringify(row).toLowerCase().includes(query),
    );
  }
  get columns(): string[] {
    return (
      this.workspace.columns ||
      Object.keys(this.rows[0] || {})
        .filter((key) => !Array.isArray(this.rows[0][key]))
        .slice(0, 7)
    );
  }
  readonly rowActions = (row: JsonRecord) => this.workflows.actions(this, row);
  has(permission?: string): boolean {
    return this.session.has(permission);
  }
  initialize(workspace: WorkspaceDefinition): void {
    this.workspace = workspace;
    this.active = workspace.id;
    this.refresh();
    this.refreshOptions();
    if (this.active === "roles")
      this.api
        .get<Permission[]>("security/permissions")
        .pipe(takeUntil(this.destroyed))
        .subscribe({
          next: (permissions) => (this.permissionCatalog = permissions),
          error: (error) => void this.fail(error),
        });
  }
  refresh(): void {
    this.loadSubscription?.unsubscribe();
    this.error = "";
    this.busy = true;
    if (this.active === "myhr" && !this.employeeId) {
      this.rows = [];
      this.busy = false;
      return;
    }
    this.loadSubscription = this.data
      .load(this.workspace)
      .pipe(takeUntil(this.destroyed))
      .subscribe({
        next: (rows) => {
          this.rows = rows;
          this.busy = false;
        },
        error: (error) => void this.fail(error),
      });
  }
  private refreshOptions(): void {
    this.lookups
      .base()
      .pipe(takeUntil(this.destroyed))
      .subscribe({
        next: (options) => {
          if (!this.task) this.options = options;
        },
        error: (error) => void this.fail(error),
      });
  }
  begin(task: Task, row: JsonRecord | null = null): void {
    this.error = "";
    this.taskError = "";
    this.message = "";
    this.selected = row;
    this.task = {
      ...task,
      initialPermissions: stringList(row?.["permissions"]),
    };
    this.optionsLoading = true;
    this.lookupSubscription?.unsubscribe();
    this.lookupSubscription = this.lookups
      .forTask(task)
      .pipe(takeUntil(this.destroyed))
      .subscribe({
        next: (options) => {
          this.options = options;
          this.optionsLoading = false;
        },
        error: (error) => {
          this.optionsLoading = false;
          void this.fail(error, true);
        },
      });
  }
  closeTask(): void {
    if (this.busy) return;
    this.lookupSubscription?.unsubscribe();
    this.task = null;
    this.taskError = "";
    this.optionsLoading = false;
  }
  act(
    label: string,
    path: string,
    fields: Field[] = [],
    defaults: JsonRecord = {},
    row: JsonRecord | null = null,
    method?: MutationMethod,
    lines?: Field[],
    lineKey?: string,
  ): void {
    this.begin({ label, path, fields, defaults, method, lines, lineKey }, row);
  }
  submit(payload: JsonRecord): void {
    if (!this.task || this.busy) return;
    const task = this.task;
    this.busy = true;
    this.taskError = "";
    const prepared = this.data.prepare
      ? this.data.prepare(task, payload)
      : of(payload);
    prepared
      .pipe(
        switchMap((body) =>
          this.api.mutate(task.method || "POST", task.path, body),
        ),
        takeUntil(this.destroyed),
      )
      .subscribe({
        next: () => {
          this.task = null;
          this.busy = false;
          this.error = "";
          this.message = task.label + " completed";
          this.refresh();
          this.refreshOptions();
          this.notices.refresh();
        },
        error: (error) => void this.fail(error, true),
      });
  }
  execute(message: string, path: string, body: JsonRecord = {}): void {
    if (this.busy) return;
    this.error = "";
    this.busy = true;
    this.api
      .post(path, body)
      .pipe(takeUntil(this.destroyed))
      .subscribe({
        next: () => {
          this.busy = false;
          this.message = message;
          this.refresh();
          this.notices.refresh();
        },
        error: (error) => void this.fail(error),
      });
  }
  showDetail(path: string): void {
    this.error = "";
    this.detailSubscription?.unsubscribe();
    this.detailSubscription = this.api
      .get<JsonValue>(path)
      .pipe(takeUntil(this.destroyed))
      .subscribe({
        next: (detail) => (this.detail = detail),
        error: (error) => void this.fail(error),
      });
  }
  download(path: string, preview = false): void {
    this.files
      .download(path, preview)
      .pipe(takeUntil(this.destroyed))
      .subscribe({ error: (error) => void this.fail(error) });
  }
  fee(): void {
    this.execute(
      "Simulated registration fee completed; excluded from revenue",
      "patient/registration-fee-demo",
    );
  }
  async fail(error: unknown, inTask = false): Promise<void> {
    const message = await readableApiError(error);
    if (this.destroyedFlag) return;
    this.busy = false;
    if (inTask) this.taskError = message;
    else this.error = message;
  }
  ngOnDestroy(): void {
    this.destroyedFlag = true;
    this.destroyed.next();
    this.destroyed.complete();
  }
}
