import { NgModule } from "@angular/core";
import { RouterModule } from "@angular/router";
import { WorkspaceModule } from "../../shared/workspace/workspace.module";
import { WorkspacePageComponent } from "../../shared/workspace/workspace-page.component";
import {
  WORKSPACE_WORKFLOWS,
  WORKSPACE_DATA_SOURCE,
} from "../../shared/workspace/workspace.models";
import { BillingApiService } from "./billing-api.service";
import { billingWorkflows } from "./billing.workflows";
@NgModule({
  imports: [
    WorkspaceModule,
    RouterModule.forChild([
      {
        path: "",
        component: WorkspacePageComponent,
        providers: [
          BillingApiService,
          { provide: WORKSPACE_DATA_SOURCE, useExisting: BillingApiService },
          { provide: WORKSPACE_WORKFLOWS, useValue: billingWorkflows },
        ],
      },
    ]),
  ],
})
export class BillingModule {}
