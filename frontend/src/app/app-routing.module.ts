import { NgModule } from "@angular/core";
import { RouterModule, Routes } from "@angular/router";
import { PortalLayoutComponent } from "./layouts/portal-layout/portal-layout.component";
import { PortalLandingComponent } from "./layouts/portal-layout/portal-landing.component";
import { PortalGuard } from "./core/auth/portal.guard";
import { FEATURE_ROUTES } from "./core/navigation/feature-routes";
const routes: Routes = [
  {
    path: "",
    pathMatch: "full",
    loadChildren: () =>
      import("./features/home/home.module").then((module) => module.HomeModule),
  },
  {
    path: "login",
    loadChildren: () =>
      import("./features/auth/auth.module").then(
        (module) => module.LoginModule,
      ),
  },
  {
    path: "register",
    loadChildren: () =>
      import("./features/auth/auth.module").then(
        (module) => module.RegisterModule,
      ),
  },
  {
    path: ":portal",
    component: PortalLayoutComponent,
    canActivate: [PortalGuard],
    children: [
      { path: "", pathMatch: "full", component: PortalLandingComponent },
      ...FEATURE_ROUTES,
      { path: "**", redirectTo: "" },
    ],
  },
];
@NgModule({
  imports: [
    RouterModule.forRoot(routes, { scrollPositionRestoration: "enabled" }),
  ],
  exports: [RouterModule],
})
export class AppRoutingModule {}
