import { NgModule } from "@angular/core";
import { RouterModule } from "@angular/router";
import { WorkspaceModule } from "../../shared/workspace/workspace.module";
import { WorkspacePageComponent } from "../../shared/workspace/workspace-page.component";
import {
  WORKSPACE_WORKFLOWS,
  WORKSPACE_DATA_SOURCE,
} from "../../shared/workspace/workspace.models";
import { HrApiService } from "./hr-api.service";
import { hrWorkflows } from "./hr.workflows";
@NgModule({
  imports: [
    WorkspaceModule,
    RouterModule.forChild([
      {
        path: "",
        component: WorkspacePageComponent,
        providers: [
          HrApiService,
          { provide: WORKSPACE_DATA_SOURCE, useExisting: HrApiService },
          { provide: WORKSPACE_WORKFLOWS, useValue: hrWorkflows },
        ],
      },
    ]),
  ],
})
export class HrModule {}
