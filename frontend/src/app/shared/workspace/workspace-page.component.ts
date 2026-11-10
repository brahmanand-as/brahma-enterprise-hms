import {
  Component,
  Inject,
  OnDestroy,
  OnInit,
  Optional,
  Type,
} from "@angular/core";
import { ActivatedRoute } from "@angular/router";
import { FormControl } from "@angular/forms";
import { Subject, takeUntil } from "rxjs";
import { WORKSPACES } from "../../core/navigation/workspaces";
import { WorkspaceFacade } from "./workspace.facade";
import { WORKSPACE_VIEWS } from "./workspace.models";
@Component({
  selector: "app-workspace-page",
  templateUrl: "./workspace-page.component.html",
  providers: [WorkspaceFacade],
})
export class WorkspacePageComponent implements OnInit, OnDestroy {
  readonly search = new FormControl("", { nonNullable: true });
  private readonly destroyed = new Subject<void>();
  customView: Type<unknown> | null = null;
  constructor(
    public readonly state: WorkspaceFacade,
    private readonly route: ActivatedRoute,
    @Optional()
    @Inject(WORKSPACE_VIEWS)
    private readonly views: Record<string, Type<unknown>> | null,
  ) {}
  ngOnInit(): void {
    const id = this.route.snapshot.data["workspaceId"] as string;
    const workspace = WORKSPACES.find((value) => value.id === id);
    if (!workspace) throw new Error("Workspace route is not configured");
    this.state.initialize(workspace);
    this.customView = this.views?.[id] || null;
    this.search.valueChanges
      .pipe(takeUntil(this.destroyed))
      .subscribe((value) => (this.state.search = value));
  }
  ngOnDestroy(): void {
    this.destroyed.next();
    this.destroyed.complete();
  }
}
