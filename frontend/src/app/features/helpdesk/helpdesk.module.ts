import { NgModule } from "@angular/core";
import { RouterModule } from "@angular/router";
import { WorkspaceModule } from "../../shared/workspace/workspace.module";
import { WorkspacePageComponent } from "../../shared/workspace/workspace-page.component";
import {
  WORKSPACE_WORKFLOWS,
  WORKSPACE_DATA_SOURCE,
} from "../../shared/workspace/workspace.models";
import { HelpdeskApiService } from "./helpdesk-api.service";
import { helpdeskWorkflows } from "./helpdesk.workflows";
@NgModule({
  imports: [
    WorkspaceModule,
    RouterModule.forChild([
      {
        path: "",
        component: WorkspacePageComponent,
        providers: [
          HelpdeskApiService,
          { provide: WORKSPACE_DATA_SOURCE, useExisting: HelpdeskApiService },
          { provide: WORKSPACE_WORKFLOWS, useValue: helpdeskWorkflows },
        ],
      },
    ]),
  ],
})
export class HelpdeskModule {}
