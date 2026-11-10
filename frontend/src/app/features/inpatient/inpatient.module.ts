import { InpatientMonitorComponent } from "./inpatient-monitor.component";
import { BedBoardComponent } from "./bed-board.component";
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
import { InpatientApiService } from "./inpatient-api.service";
import { inpatientWorkflows } from "./inpatient.workflows";
@NgModule({
  declarations: [BedBoardComponent, InpatientMonitorComponent],
  imports: [
    SharedModule,
    WorkspaceModule,
    RouterModule.forChild([
      {
        path: "",
        component: WorkspacePageComponent,
        providers: [
          {
            provide: WORKSPACE_VIEWS,
            useValue: {
              beds: BedBoardComponent,
              admissions: InpatientMonitorComponent,
            },
          },
          InpatientApiService,
          { provide: WORKSPACE_DATA_SOURCE, useExisting: InpatientApiService },
          { provide: WORKSPACE_WORKFLOWS, useValue: inpatientWorkflows },
        ],
      },
    ]),
  ],
})
export class InpatientModule {}
