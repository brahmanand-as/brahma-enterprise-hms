import { Component } from "@angular/core";
import { AuthService } from "./core/auth/auth.service";
@Component({
  selector: "app-root",
  template: "<router-outlet></router-outlet>",
})
export class AppComponent {
  constructor(_auth: AuthService) {}
}
