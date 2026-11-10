import { NgModule } from "@angular/core";
import { RouterModule } from "@angular/router";
import { WorkspaceModule } from "../../shared/workspace/workspace.module";
import { WorkspacePageComponent } from "../../shared/workspace/workspace-page.component";
import {
  WORKSPACE_WORKFLOWS,
  WORKSPACE_DATA_SOURCE,
} from "../../shared/workspace/workspace.models";
import { NotificationsApiService } from "./notifications-api.service";
import { notificationsWorkflows } from "./notifications.workflows";
@NgModule({
  imports: [
    WorkspaceModule,
    RouterModule.forChild([
      {
        path: "",
        component: WorkspacePageComponent,
        providers: [
          NotificationsApiService,
          {
            provide: WORKSPACE_DATA_SOURCE,
            useExisting: NotificationsApiService,
          },
          { provide: WORKSPACE_WORKFLOWS, useValue: notificationsWorkflows },
        ],
      },
    ]),
  ],
})
export class NotificationsModule {}
