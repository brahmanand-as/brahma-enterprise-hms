import { NgModule } from "@angular/core";
import { RouterModule } from "@angular/router";
import { WorkspaceModule } from "../../shared/workspace/workspace.module";
import { WorkspacePageComponent } from "../../shared/workspace/workspace-page.component";
import {
  WORKSPACE_WORKFLOWS,
  WORKSPACE_DATA_SOURCE,
} from "../../shared/workspace/workspace.models";
import { AdministrationApiService } from "./administration-api.service";
import { administrationWorkflows } from "./administration.workflows";
@NgModule({
  imports: [
    WorkspaceModule,
    RouterModule.forChild([
      {
        path: "",
        component: WorkspacePageComponent,
        providers: [
          AdministrationApiService,
          {
            provide: WORKSPACE_DATA_SOURCE,
            useExisting: AdministrationApiService,
          },
          { provide: WORKSPACE_WORKFLOWS, useValue: administrationWorkflows },
        ],
      },
    ]),
  ],
})
export class AdministrationModule {}
