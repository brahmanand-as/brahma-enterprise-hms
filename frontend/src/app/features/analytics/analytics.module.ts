import { NgModule } from "@angular/core";
import { RouterModule } from "@angular/router";
import { SharedModule } from "../../shared/shared.module";
import { AnalyticsComponent } from "./analytics.component";
import { AnalyticsApiService } from "./analytics-api.service";
@NgModule({
  declarations: [AnalyticsComponent],
  providers: [AnalyticsApiService],
  imports: [
    SharedModule,
    RouterModule.forChild([{ path: "", component: AnalyticsComponent }]),
  ],
})
export class AnalyticsModule {}
