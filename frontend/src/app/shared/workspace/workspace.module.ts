import { NgModule } from "@angular/core";
import { SharedModule } from "../shared.module";
import { WorkspacePageComponent } from "./workspace-page.component";
@NgModule({
  declarations: [WorkspacePageComponent],
  imports: [SharedModule],
  exports: [WorkspacePageComponent],
})
export class WorkspaceModule {}
