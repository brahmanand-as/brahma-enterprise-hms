import { NgModule } from "@angular/core";
import { RouterModule } from "@angular/router";
import { WorkspaceModule } from "../../shared/workspace/workspace.module";
import { WorkspacePageComponent } from "../../shared/workspace/workspace-page.component";
import {
  WORKSPACE_WORKFLOWS,
  WORKSPACE_DATA_SOURCE,
} from "../../shared/workspace/workspace.models";
import { AppointmentsApiService } from "./appointments-api.service";
import { appointmentsWorkflows } from "./appointments.workflows";
@NgModule({
  imports: [
    WorkspaceModule,
    RouterModule.forChild([
      {
        path: "",
        component: WorkspacePageComponent,
        providers: [
          AppointmentsApiService,
          {
            provide: WORKSPACE_DATA_SOURCE,
            useExisting: AppointmentsApiService,
          },
          { provide: WORKSPACE_WORKFLOWS, useValue: appointmentsWorkflows },
        ],
      },
    ]),
  ],
})
export class AppointmentsModule {}
