import { DoctorDashboardComponent } from "./doctor-dashboard.component";
import { NgModule } from "@angular/core";
import { RouterModule } from "@angular/router";
import { SharedModule } from "../../shared/shared.module";
import { DashboardComponent } from "./dashboard.component";
@NgModule({
  declarations: [DashboardComponent, DoctorDashboardComponent],
  imports: [
    SharedModule,
    RouterModule.forChild([{ path: "", component: DashboardComponent }]),
  ],
})
export class DashboardModule {}
