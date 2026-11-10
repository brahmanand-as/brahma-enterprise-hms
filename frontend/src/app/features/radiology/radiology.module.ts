import { NgModule } from "@angular/core";
import { RouterModule } from "@angular/router";
import { WorkspaceModule } from "../../shared/workspace/workspace.module";
import { WorkspacePageComponent } from "../../shared/workspace/workspace-page.component";
import {
  WORKSPACE_WORKFLOWS,
  WORKSPACE_DATA_SOURCE,
} from "../../shared/workspace/workspace.models";
import { RadiologyApiService } from "./radiology-api.service";
import { radiologyWorkflows } from "./radiology.workflows";
@NgModule({
  imports: [
    WorkspaceModule,
    RouterModule.forChild([
      {
        path: "",
        component: WorkspacePageComponent,
        providers: [
          RadiologyApiService,
          { provide: WORKSPACE_DATA_SOURCE, useExisting: RadiologyApiService },
          { provide: WORKSPACE_WORKFLOWS, useValue: radiologyWorkflows },
        ],
      },
    ]),
  ],
})
export class RadiologyModule {}
