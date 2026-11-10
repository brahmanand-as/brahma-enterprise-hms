import { Component } from "@angular/core";
@Component({
  selector: "app-auth-layout",
  template: `<div class="auth-page">
    <section class="auth-story">
      <a class="brand" routerLink="/"
        >✚ <span>AROGYA<small>ENTERPRISE HOSPITAL</small></span></a
      >
      <div>
        <span class="eyebrow">CONNECTED CARE STARTS HERE</span>
        <h1>One team.<br />One patient journey.</h1>
        <p>Secure, role-aware workspaces for every hospital department.</p>
      </div>
      <small
        >Patient information is available only to authorized care teams.</small
      >
    </section>
    <router-outlet></router-outlet>
  </div>`,
})
export class AuthLayoutComponent {}
