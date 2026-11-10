# Application architecture

The application is a modular monolith. HTTP controllers delegate to constructor-injected
services; repositories own JPA persistence and JDBC SQL. Validated DTOs and explicit
mappers separate HTTP contracts from entities. The entry point remains
`com.brahma.arogya.ArogyaApplication`.

| Area | Main components |
|---|---|
| Authentication and permissions | SecurityConfig, JwtService/JwtFilter, AccessPolicy, SecurityAdministration, LoginThrottle |
| Care scope | CareScope, ScopedRequestAdvice, ScopedResponseAdvice, WorkflowInterceptors, CareAssignmentService |
| Scheduling | SchedulingService, AppointmentManagementService, StaffingService |
| OPD | ClinicalWorkflowService, ClinicalOrdersService, VisitBillingService, PatientTimelineService |
| Inpatient/nursing | InpatientService, WardService, BedChargeService |
| Pharmacy/stock | PharmacyService, inventory_batches, batch_movements, ward_stock, dispense_allocations |
| Finance | RevenueLedger, FinancialService, invoice_items/payments/refunds/adjustments/return credits |
| Insurance | InsuranceIntakeService, EnterpriseOperationsService, InsuranceReconciliationService |
| Documents | ClinicalDocumentService, immutable issued_documents snapshots, bundled PDF font/QR |
| HR | EmployeeService, StaffPortalService, StaffingService |
| Helpdesk/notifications | EnterprisePortalService, HelpdeskConversationService, CoordinationService, PersonalWorkspaceService |
| Analytics | AnalyticsReportService, monthly comparison, OpenXML/PDF exports |
| UI | Angular NgModules with lazy feature routes; core auth/HTTP services, shared reactive forms/tables and feature-owned workflows |

## Transaction boundaries

Slot reservation/rescheduling, bed allocation/transfer, purchase receipt, FEFO
stock allocation, medication outcome, payment/adjustment, insurer allocation and
settlement have their transaction boundaries on services. Pessimistic row locks serialize contested resources;
unique constraints additionally protect slot reservations, active admissions,
verified electronic references and reconciliation references.

Payment amounts are BigDecimal and stored as decimal columns. Dummy registration
payments are in a separate table. Pending insurer allocations reserve the payer's
share; actual reconciled insurer payments enter the payment ledger once. Patient
refunds cannot refund insurer receipts.

Security defaults to deny for unknown workflow routes. Permissions are loaded
from database role grants on every JWT-authenticated request. Patient endpoints
also require ownership; clinician requests/responses are restricted to assigned
care teams. Workflow-specific transition checks remain on the server. Generic
CRUD writes that bypass the workflow are disabled.

HTTP mutation outcomes are audited without recording credential/request bodies.
Domain ledgers additionally retain quantities, state decisions, references and
actors. Issued clinical documents have no update endpoint.

The new database schema is defined by the Flyway files under
`backend/src/main/resources/db/migration`. Earlier tables are retained;
unused legacy tables do not imply active workflow functionality. All added data
is generated synthetic seed data or entered through the new workflow APIs.
