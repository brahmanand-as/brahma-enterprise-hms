import { NgModule } from "@angular/core";
import { RouterModule } from "@angular/router";
import { WorkspaceModule } from "../../shared/workspace/workspace.module";
import { WorkspacePageComponent } from "../../shared/workspace/workspace-page.component";
import {
  WORKSPACE_WORKFLOWS,
  WORKSPACE_DATA_SOURCE,
} from "../../shared/workspace/workspace.models";
import { LaboratoryApiService } from "./laboratory-api.service";
import { laboratoryWorkflows } from "./laboratory.workflows";
@NgModule({
  imports: [
    WorkspaceModule,
    RouterModule.forChild([
      {
        path: "",
        component: WorkspacePageComponent,
        providers: [
          LaboratoryApiService,
          { provide: WORKSPACE_DATA_SOURCE, useExisting: LaboratoryApiService },
          { provide: WORKSPACE_WORKFLOWS, useValue: laboratoryWorkflows },
        ],
      },
    ]),
  ],
})
export class LaboratoryModule {}
