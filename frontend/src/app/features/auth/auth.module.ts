import { NgModule } from "@angular/core";
import { RouterModule } from "@angular/router";
import { SharedModule } from "../../shared/shared.module";
import { AuthLayoutComponent } from "../../layouts/auth-layout/auth-layout.component";
import { LoginComponent } from "./login.component";
import { RegisterComponent } from "./register.component";
@NgModule({
  declarations: [AuthLayoutComponent, LoginComponent, RegisterComponent],
  imports: [SharedModule],
  exports: [AuthLayoutComponent, LoginComponent, RegisterComponent],
})
class AuthComponentsModule {}
@NgModule({
  imports: [
    AuthComponentsModule,
    RouterModule.forChild([
      {
        path: "",
        component: AuthLayoutComponent,
        children: [{ path: "", component: LoginComponent }],
      },
    ]),
  ],
})
export class LoginModule {}
@NgModule({
  imports: [
    AuthComponentsModule,
    RouterModule.forChild([
      {
        path: "",
        component: AuthLayoutComponent,
        children: [{ path: "", component: RegisterComponent }],
      },
    ]),
  ],
})
export class RegisterModule {}
