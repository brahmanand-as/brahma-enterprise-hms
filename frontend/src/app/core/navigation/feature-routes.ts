import { Routes } from "@angular/router";
import { WorkspaceGuard } from "../auth/workspace.guard";
export const FEATURE_ROUTES: Routes = [
  {
    path: "overview",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "overview" },
    loadChildren: () =>
      import("../../features/dashboard/dashboard.module").then(
        (module) => module.DashboardModule,
      ),
  },
  {
    path: "patients",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "patients" },
    loadChildren: () =>
      import("../../features/patient/patient.module").then(
        (module) => module.PatientModule,
      ),
  },
  {
    path: "appointments",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "appointments" },
    loadChildren: () =>
      import("../../features/appointments/appointments.module").then(
        (module) => module.AppointmentsModule,
      ),
  },
  {
    path: "scheduling",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "scheduling" },
    loadChildren: () =>
      import("../../features/appointments/appointments.module").then(
        (module) => module.AppointmentsModule,
      ),
  },
  {
    path: "rescheduling",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "rescheduling" },
    loadChildren: () =>
      import("../../features/appointments/appointments.module").then(
        (module) => module.AppointmentsModule,
      ),
  },
  {
    path: "visits",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "visits" },
    loadChildren: () =>
      import("../../features/clinical/clinical.module").then(
        (module) => module.ClinicalModule,
      ),
  },
  {
    path: "prescriptions",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "prescriptions" },
    loadChildren: () =>
      import("../../features/clinical/clinical.module").then(
        (module) => module.ClinicalModule,
      ),
  },
  {
    path: "beds",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "beds" },
    loadChildren: () =>
      import("../../features/inpatient/inpatient.module").then(
        (module) => module.InpatientModule,
      ),
  },
  {
    path: "admissions",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "admissions" },
    loadChildren: () =>
      import("../../features/inpatient/inpatient.module").then(
        (module) => module.InpatientModule,
      ),
  },
  {
    path: "recommendations",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "recommendations" },
    loadChildren: () =>
      import("../../features/inpatient/inpatient.module").then(
        (module) => module.InpatientModule,
      ),
  },
  {
    path: "medication",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "medication" },
    loadChildren: () =>
      import("../../features/inpatient/inpatient.module").then(
        (module) => module.InpatientModule,
      ),
  },
  {
    path: "emar",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "emar" },
    loadChildren: () =>
      import("../../features/inpatient/inpatient.module").then(
        (module) => module.InpatientModule,
      ),
  },
  {
    path: "nursing",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "nursing" },
    loadChildren: () =>
      import("../../features/inpatient/inpatient.module").then(
        (module) => module.InpatientModule,
      ),
  },
  {
    path: "laboratory",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "laboratory" },
    loadChildren: () =>
      import("../../features/laboratory/laboratory.module").then(
        (module) => module.LaboratoryModule,
      ),
  },
  {
    path: "labcatalog",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "labcatalog" },
    loadChildren: () =>
      import("../../features/laboratory/laboratory.module").then(
        (module) => module.LaboratoryModule,
      ),
  },
  {
    path: "dispatches",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "dispatches" },
    loadChildren: () =>
      import("../../features/laboratory/laboratory.module").then(
        (module) => module.LaboratoryModule,
      ),
  },
  {
    path: "radiology",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "radiology" },
    loadChildren: () =>
      import("../../features/radiology/radiology.module").then(
        (module) => module.RadiologyModule,
      ),
  },
  {
    path: "inventory",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "inventory" },
    loadChildren: () =>
      import("../../features/pharmacy/pharmacy.module").then(
        (module) => module.PharmacyModule,
      ),
  },
  {
    path: "batches",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "batches" },
    loadChildren: () =>
      import("../../features/pharmacy/pharmacy.module").then(
        (module) => module.PharmacyModule,
      ),
  },
  {
    path: "purchases",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "purchases" },
    loadChildren: () =>
      import("../../features/pharmacy/pharmacy.module").then(
        (module) => module.PharmacyModule,
      ),
  },
  {
    path: "suppliers",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "suppliers" },
    loadChildren: () =>
      import("../../features/pharmacy/pharmacy.module").then(
        (module) => module.PharmacyModule,
      ),
  },
  {
    path: "dispenses",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "dispenses" },
    loadChildren: () =>
      import("../../features/pharmacy/pharmacy.module").then(
        (module) => module.PharmacyModule,
      ),
  },
  {
    path: "wardstock",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "wardstock" },
    loadChildren: () =>
      import("../../features/pharmacy/pharmacy.module").then(
        (module) => module.PharmacyModule,
      ),
  },
  {
    path: "consumables",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "consumables" },
    loadChildren: () =>
      import("../../features/pharmacy/pharmacy.module").then(
        (module) => module.PharmacyModule,
      ),
  },
  {
    path: "movements",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "movements" },
    loadChildren: () =>
      import("../../features/pharmacy/pharmacy.module").then(
        (module) => module.PharmacyModule,
      ),
  },
  {
    path: "billing",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "billing" },
    loadChildren: () =>
      import("../../features/billing/billing.module").then(
        (module) => module.BillingModule,
      ),
  },
  {
    path: "creditqueue",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "creditqueue" },
    loadChildren: () =>
      import("../../features/billing/billing.module").then(
        (module) => module.BillingModule,
      ),
  },
  {
    path: "insurance",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "insurance" },
    loadChildren: () =>
      import("../../features/insurance/insurance.module").then(
        (module) => module.InsuranceModule,
      ),
  },
  {
    path: "enhancements",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "enhancements" },
    loadChildren: () =>
      import("../../features/insurance/insurance.module").then(
        (module) => module.InsuranceModule,
      ),
  },
  {
    path: "roster",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "roster" },
    loadChildren: () =>
      import("../../features/hr/hr.module").then((module) => module.HrModule),
  },
  {
    path: "swaps",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "swaps" },
    loadChildren: () =>
      import("../../features/hr/hr.module").then((module) => module.HrModule),
  },
  {
    path: "employees",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "employees" },
    loadChildren: () =>
      import("../../features/hr/hr.module").then((module) => module.HrModule),
  },
  {
    path: "staff",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "staff" },
    loadChildren: () =>
      import("../../features/hr/hr.module").then((module) => module.HrModule),
  },
  {
    path: "attendance",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "attendance" },
    loadChildren: () =>
      import("../../features/hr/hr.module").then((module) => module.HrModule),
  },
  {
    path: "leave",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "leave" },
    loadChildren: () =>
      import("../../features/hr/hr.module").then((module) => module.HrModule),
  },
  {
    path: "myhr",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "myhr" },
    loadChildren: () =>
      import("../../features/hr/hr.module").then((module) => module.HrModule),
  },
  {
    path: "heads",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "heads" },
    loadChildren: () =>
      import("../../features/hr/hr.module").then((module) => module.HrModule),
  },
  {
    path: "payroll",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "payroll" },
    loadChildren: () =>
      import("../../features/hr/hr.module").then((module) => module.HrModule),
  },
  {
    path: "profiles",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "profiles" },
    loadChildren: () =>
      import("../../features/hr/hr.module").then((module) => module.HrModule),
  },
  {
    path: "departments",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "departments" },
    loadChildren: () =>
      import("../../features/administration/administration.module").then(
        (module) => module.AdministrationModule,
      ),
  },
  {
    path: "careteams",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "careteams" },
    loadChildren: () =>
      import("../../features/administration/administration.module").then(
        (module) => module.AdministrationModule,
      ),
  },
  {
    path: "roles",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "roles" },
    loadChildren: () =>
      import("../../features/administration/administration.module").then(
        (module) => module.AdministrationModule,
      ),
  },
  {
    path: "accounts",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "accounts" },
    loadChildren: () =>
      import("../../features/administration/administration.module").then(
        (module) => module.AdministrationModule,
      ),
  },
  {
    path: "settings",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "settings" },
    loadChildren: () =>
      import("../../features/administration/administration.module").then(
        (module) => module.AdministrationModule,
      ),
  },
  {
    path: "audit",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "audit" },
    loadChildren: () =>
      import("../../features/administration/administration.module").then(
        (module) => module.AdministrationModule,
      ),
  },
  {
    path: "helpdesk",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "helpdesk" },
    loadChildren: () =>
      import("../../features/helpdesk/helpdesk.module").then(
        (module) => module.HelpdeskModule,
      ),
  },
  {
    path: "documents",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "documents" },
    loadChildren: () =>
      import("../../features/documents/documents.module").then(
        (module) => module.DocumentsModule,
      ),
  },
  {
    path: "notifications",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "notifications" },
    loadChildren: () =>
      import("../../features/notifications/notifications.module").then(
        (module) => module.NotificationsModule,
      ),
  },
  {
    path: "myprofile",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "myprofile" },
    loadChildren: () =>
      import("../../features/patient-portal/patient-portal.module").then(
        (module) => module.PatientPortalModule,
      ),
  },
  {
    path: "myappointments",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "myappointments" },
    loadChildren: () =>
      import("../../features/patient-portal/patient-portal.module").then(
        (module) => module.PatientPortalModule,
      ),
  },
  {
    path: "myinvoices",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "myinvoices" },
    loadChildren: () =>
      import("../../features/patient-portal/patient-portal.module").then(
        (module) => module.PatientPortalModule,
      ),
  },
  {
    path: "myreports",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "myreports" },
    loadChildren: () =>
      import("../../features/patient-portal/patient-portal.module").then(
        (module) => module.PatientPortalModule,
      ),
  },
  {
    path: "myprescriptions",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "myprescriptions" },
    loadChildren: () =>
      import("../../features/patient-portal/patient-portal.module").then(
        (module) => module.PatientPortalModule,
      ),
  },
  {
    path: "mydocuments",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "mydocuments" },
    loadChildren: () =>
      import("../../features/patient-portal/patient-portal.module").then(
        (module) => module.PatientPortalModule,
      ),
  },
  {
    path: "analytics",
    canActivate: [WorkspaceGuard],
    data: { workspaceId: "analytics" },
    loadChildren: () =>
      import("../../features/analytics/analytics.module").then(
        (module) => module.AnalyticsModule,
      ),
  },
];
