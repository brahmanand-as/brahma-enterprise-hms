# Brahma Enterprise HMS — Enterprise Hospital Management System

An enterprise-scale hospital management system (demo hospital: Arogya) covering the full patient journey — appointments, OPD, inpatient/ICU care, nursing, laboratory, radiology, pharmacy, inventory, billing, insurance, HR/payroll, helpdesk and analytics — with 15 role-based portals.

**Tech stack:** Java 21 · Spring Boot 4 · Spring Security · JWT · Spring Data JPA · MySQL · Flyway · Swagger / OpenAPI · Angular 16 · Docker Compose · JUnit 5 · Mockito · Playwright

---

## Highlights

- **Modular monolith** with 20+ domain modules: thin controllers, services, repositories, validated DTOs and explicit mappers
- **Security:** JWT authentication, database-driven role permissions checked on every request, custom roles, login throttling, token revocation on account disable/reset, default-deny for unknown routes
- **Data scoping:** clinicians only see patients in their assigned care team; patients only see their own records
- **Concurrency safety:** service-level transactions, pessimistic row locks and unique constraints prevent double-booked slots and beds
- **Billing & insurance:** itemised invoices, split/partial payments, refunds, cashless pre-authorisation and partial insurer settlements, all with `BigDecimal`
- **Pharmacy & inventory:** suppliers, purchase orders, goods receipts, batch/expiry tracking and FEFO dispensing
- **Documents:** immutable PDF clinical reports (Apache PDFBox) with QR verification
- **Analytics:** monthly comparison dashboards with XLSX and PDF export
- **Schema versioning:** Flyway migrations; Hibernate validates the schema on start-up
- **API docs:** Swagger UI with JWT Bearer authorization
- **Testing:** JUnit + Mockito + MockMvc unit tests, a Python API smoke-test suite, and Playwright browser tests

## Portals

Admin · Doctor · Nurse · Reception · Admission (IPD) · Lab Technician · Lab Supervisor · Pathologist · Radiology · Pharmacy · Billing · Insurance · HR · Helpdesk · Patient

---

## Start the complete application

Requirements: Docker with Compose and internet access for the initial image build.

1. Copy `.env.example` to `.env`.
2. Replace every password/secret placeholder. The administrator and demo passwords
   require at least 12 characters; the JWT secret requires at least 32 bytes.
3. Run `docker compose up --build` from this directory.
4. Open `http://localhost:4200`. Sign in as `admin` with `HMS_ADMIN_PASSWORD`.

On Windows PowerShell, the copy command is `Copy-Item .env.example .env`.

The database is **arogya_hms_enterprise**, stored in its own Compose volume.
No old patient records are imported, and the old hospital database is never used.
Flyway creates and versions the new schema; Hibernate validates it. Do not alter
applied schema migrations. The package contains source and dependency lockfiles;
Docker builds both applications. Dependency folders and local database files are
excluded.

## Synthetic demonstration accounts

Set `SPRING_PROFILES_ACTIVE=demo` and `HMS_DEMO_PASSWORD` in `.env` to enable the
empty-database synthetic seed. Default `.env.example` uses the demo profile.

| Workspace | Username |
|---|---|
| Administrator | `admin` (uses `HMS_ADMIN_PASSWORD`) |
| Patient | `demo.patient` |
| Doctor | `demo.doctor` |
| Nurse | `demo.nurse` |
| Reception | `demo.receptionist` |
| Admission staff | `demo.ipd` |
| Laboratory technician | `demo.lab_technician` |
| Laboratory supervisor | `demo.lab_supervisor` |
| Pathologist | `demo.pathologist` |
| Radiology | `demo.radiologist` |
| Pharmacy | `demo.pharmacist` |
| Billing | `demo.billing` |
| Insurance | `demo.insurance` |
| HR | `demo.hr` |
| Helpdesk | `demo.helpdesk` |

All `demo.*` accounts use your `HMS_DEMO_PASSWORD`; no shared password is hardcoded.
The seed runs only when the patient table is empty. Restarting does not reset
passwords or existing data. Set `SPRING_PROFILES_ACTIVE=default` for an unseeded
installation. Configure actual hospital settings, staff and rates before use.

## Independent portal origins

To run separate browser origins for the portals:

```sh
docker compose -f compose.yaml -f compose.portals.yaml up --build
```

| Portal | URL |
|---|---|
| Shared entry | `http://localhost:4200` |
| Admin | `http://localhost:4301/admin` |
| Doctor | `http://localhost:4302/doctor` |
| Nurse | `http://localhost:4303/nurse` |
| Reception | `http://localhost:4304/receptionist` |
| Laboratory | `http://localhost:4305/lab_technician` |
| Pharmacy | `http://localhost:4306/pharmacist` |
| HR | `http://localhost:4307/hr` |
| Patient | `http://localhost:4308/patient` |

Each origin proxies its own `/api`. JWTs remain in tab memory, rather than shared
browser storage. A reload requires sign-in. Each account's role and permissions
are checked by the backend; an origin is an independent entry point, not an
additional authorization boundary. Account disable/reset and sign-out revoke
existing tokens for that account.

## Local development

Requirements: JDK 21, Maven 3.9+, MySQL 8.x, Node 18.19.x/npm for Angular 16.2.
Create an empty `arogya_hms_enterprise`, then configure `DB_HOST`, `DB_USERNAME`,
`DB_PASSWORD`, `HMS_ADMIN_PASSWORD`, `HMS_JWT_SECRET` and optional demo variables.
The configured hospital timezone is Asia/Kolkata.

```sh
cd backend
mvn verify
mvn spring-boot:run
```

In another terminal:

```sh
cd frontend
npm ci
npm start
```

The development proxy forwards `/api` to localhost:8080. API documentation is at
`http://localhost:8080/swagger-ui.html`; obtain a token with `/api/auth/login`, then
use Swagger's Bearer authorization. Documentation is public in this development
configuration and can be disabled through Springdoc settings.

## Verify the workflows

Against a **fresh synthetic demo database**, set `HMS_ADMIN_PASSWORD`,
`HMS_DEMO_PASSWORD`, and optionally `HMS_TEST_URL` (defaults to localhost:8080/api):

```sh
python scripts/enterprise_smoke.py
```

This writes synthetic records and tests the connected workflows and rejected
operations. It includes the basic smoke scenarios and must not be rerun against
real hospital data. Results are written to `enterprise-smoke-result.json`.

Optional browser acceptance uses Node 22+ at the project root (separate from the
Angular build toolchain):

```sh
npm ci
npx playwright install chromium
npm run test:browser
```

Set `HMS_UI_URL` if the UI is not at localhost:4200. The browser runner uses the
same password variables and checks role navigation, patient registration and
mobile layout. It writes local QA screenshots/results under `tools/` and the
project root. Those generated QA files are not production assets.

## Documentation

- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) — components, transaction boundaries and security model
- [docs/WORKFLOW_GUIDE.md](docs/WORKFLOW_GUIDE.md) — setup and end-to-end workflows
- [docs/LIMITATIONS.md](docs/LIMITATIONS.md) — integration boundaries and deployment notes

---

## Author

**Brahmananda S** — Java Full Stack Developer, Bangalore
[LinkedIn](https://www.linkedin.com/in/brahmananda) · brahmananda.a.s@gmail.com
