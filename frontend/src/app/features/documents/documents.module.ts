import { NgModule } from "@angular/core";
import { RouterModule } from "@angular/router";
import { WorkspaceModule } from "../../shared/workspace/workspace.module";
import { WorkspacePageComponent } from "../../shared/workspace/workspace-page.component";
import {
  WORKSPACE_WORKFLOWS,
  WORKSPACE_DATA_SOURCE,
} from "../../shared/workspace/workspace.models";
import { DocumentsApiService } from "./documents-api.service";
import { documentsWorkflows } from "./documents.workflows";
@NgModule({
  imports: [
    WorkspaceModule,
    RouterModule.forChild([
      {
        path: "",
        component: WorkspacePageComponent,
        providers: [
          DocumentsApiService,
          { provide: WORKSPACE_DATA_SOURCE, useExisting: DocumentsApiService },
          { provide: WORKSPACE_WORKFLOWS, useValue: documentsWorkflows },
        ],
      },
    ]),
  ],
})
export class DocumentsModule {}
