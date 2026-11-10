import { OpdBoardComponent } from "./opd-board.component";
import { SharedModule } from "../../shared/shared.module";
import { WORKSPACE_VIEWS } from "../../shared/workspace/workspace.models";
import { NgModule } from "@angular/core";
import { RouterModule } from "@angular/router";
import { WorkspaceModule } from "../../shared/workspace/workspace.module";
import { WorkspacePageComponent } from "../../shared/workspace/workspace-page.component";
import {
  WORKSPACE_WORKFLOWS,
  WORKSPACE_DATA_SOURCE,
} from "../../shared/workspace/workspace.models";
import { ClinicalApiService } from "./clinical-api.service";
import { clinicalWorkflows } from "./clinical.workflows";
@NgModule({
  declarations: [OpdBoardComponent],
  imports: [
    SharedModule,
    WorkspaceModule,
    RouterModule.forChild([
      {
        path: "",
        component: WorkspacePageComponent,
        providers: [
          { provide: WORKSPACE_VIEWS, useValue: { visits: OpdBoardComponent } },
          ClinicalApiService,
          { provide: WORKSPACE_DATA_SOURCE, useExisting: ClinicalApiService },
          { provide: WORKSPACE_WORKFLOWS, useValue: clinicalWorkflows },
        ],
      },
    ]),
  ],
})
export class ClinicalModule {}
