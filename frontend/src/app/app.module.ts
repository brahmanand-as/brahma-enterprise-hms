import { NgModule } from "@angular/core";
import { BrowserModule } from "@angular/platform-browser";
import { HTTP_INTERCEPTORS, HttpClientModule } from "@angular/common/http";
import { SharedModule } from "./shared/shared.module";
import { AppRoutingModule } from "./app-routing.module";
import { AppComponent } from "./app.component";
import { PortalLayoutComponent } from "./layouts/portal-layout/portal-layout.component";
import { PortalLandingComponent } from "./layouts/portal-layout/portal-landing.component";
import { AuthInterceptor } from "./core/http/auth.interceptor";
@NgModule({
  declarations: [AppComponent, PortalLayoutComponent, PortalLandingComponent],
  imports: [BrowserModule, HttpClientModule, SharedModule, AppRoutingModule],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
