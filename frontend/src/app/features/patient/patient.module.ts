import { NgModule } from "@angular/core";
import { RouterModule } from "@angular/router";
import { WorkspaceModule } from "../../shared/workspace/workspace.module";
import { WorkspacePageComponent } from "../../shared/workspace/workspace-page.component";
import {
  WORKSPACE_WORKFLOWS,
  WORKSPACE_DATA_SOURCE,
} from "../../shared/workspace/workspace.models";
import { PatientApiService } from "./patient-api.service";
import { patientWorkflows } from "./patient.workflows";
@NgModule({
  imports: [
    WorkspaceModule,
    RouterModule.forChild([
      {
        path: "",
        component: WorkspacePageComponent,
        providers: [
          PatientApiService,
          { provide: WORKSPACE_DATA_SOURCE, useExisting: PatientApiService },
          { provide: WORKSPACE_WORKFLOWS, useValue: patientWorkflows },
        ],
      },
    ]),
  ],
})
export class PatientModule {}
