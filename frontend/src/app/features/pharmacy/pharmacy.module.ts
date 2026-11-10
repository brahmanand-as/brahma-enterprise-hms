import { NgModule } from "@angular/core";
import { RouterModule } from "@angular/router";
import { WorkspaceModule } from "../../shared/workspace/workspace.module";
import { WorkspacePageComponent } from "../../shared/workspace/workspace-page.component";
import {
  WORKSPACE_WORKFLOWS,
  WORKSPACE_DATA_SOURCE,
} from "../../shared/workspace/workspace.models";
import { PharmacyApiService } from "./pharmacy-api.service";
import { pharmacyWorkflows } from "./pharmacy.workflows";
@NgModule({
  imports: [
    WorkspaceModule,
    RouterModule.forChild([
      {
        path: "",
        component: WorkspacePageComponent,
        providers: [
          PharmacyApiService,
          { provide: WORKSPACE_DATA_SOURCE, useExisting: PharmacyApiService },
          { provide: WORKSPACE_WORKFLOWS, useValue: pharmacyWorkflows },
        ],
      },
    ]),
  ],
})
export class PharmacyModule {}
