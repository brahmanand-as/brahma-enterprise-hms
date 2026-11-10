import { NgModule } from "@angular/core";
import { RouterModule } from "@angular/router";
import { WorkspaceModule } from "../../shared/workspace/workspace.module";
import { WorkspacePageComponent } from "../../shared/workspace/workspace-page.component";
import {
  WORKSPACE_WORKFLOWS,
  WORKSPACE_DATA_SOURCE,
} from "../../shared/workspace/workspace.models";
import { PatientPortalApiService } from "./patient-portal-api.service";
import { patientPortalWorkflows } from "./patient-portal.workflows";
@NgModule({
  imports: [
    WorkspaceModule,
    RouterModule.forChild([
      {
        path: "",
        component: WorkspacePageComponent,
        providers: [
          PatientPortalApiService,
          {
            provide: WORKSPACE_DATA_SOURCE,
            useExisting: PatientPortalApiService,
          },
          { provide: WORKSPACE_WORKFLOWS, useValue: patientPortalWorkflows },
        ],
      },
    ]),
  ],
})
export class PatientPortalModule {}
