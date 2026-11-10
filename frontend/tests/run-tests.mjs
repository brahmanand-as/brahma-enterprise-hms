import fs from "node:fs";
import path from "node:path";
import assert from "node:assert/strict";
import { pathToFileURL } from "node:url";
import ts from "typescript";
// Angular's partially compiled forms package needs its JIT compiler in this Node-only test host.
await import("@angular/compiler");
const root = process.cwd(),
  source = path.join(root, "src/app"),
  output = path.join(root, ".test-build");
fs.rmSync(output, { recursive: true, force: true });
fs.mkdirSync(output, { recursive: true });
fs.writeFileSync(path.join(output, "package.json"), '{"type":"module"}');
function walk(directory) {
  return fs
    .readdirSync(directory, { withFileTypes: true })
    .flatMap((entry) =>
      entry.isDirectory()
        ? walk(path.join(directory, entry.name))
        : [path.join(directory, entry.name)],
    );
}
for (const file of walk(source).filter((file) => file.endsWith(".ts"))) {
  const relative = path.relative(source, file),
    destination = path.join(output, relative.replace(/\.ts$/, ".js"));
  let code = ts.transpileModule(fs.readFileSync(file, "utf8"), {
    compilerOptions: {
      target: ts.ScriptTarget.ES2022,
      module: ts.ModuleKind.ES2022,
      experimentalDecorators: true,
    },
  }).outputText;
  code = code
    .replace(/(from\s*['"])(\.[^'"]+)(['"])/g, "$1$2.js$3")
    .replace(/(import\(['"])(\.[^'"]+)(['"]\))/g, "$1$2.js$3");
  fs.mkdirSync(path.dirname(destination), { recursive: true });
  fs.writeFileSync(destination, code);
}
const load = (name) =>
  import(pathToFileURL(path.join(output, name + ".js")).href);
const results = [];
async function check(name, run) {
  await run();
  results.push(name);
  console.log("PASS", name);
}
const fixture = JSON.parse(
  fs.readFileSync("tests/fixtures/workflow-contract.json", "utf8"),
);
const { WORKSPACES } = await load("core/navigation/workspaces");
const { visibleWorkspaces, hasPermission } = await load(
  "core/auth/permission.utils",
);
const { field, displayValue } = await load("shared/utils/record.utils");
const { createTaskForm, buildPayload, validatorsFor } = await load(
  "shared/forms/task-form.factory",
);
const { FormControl } = await import("@angular/forms");
const { SessionStore } = await load("core/auth/session.store");
const { AuthInterceptor } = await load("core/http/auth.interceptor");
const { HttpRequest, HttpResponse, HttpErrorResponse } = await import(
  "@angular/common/http"
);
const { of, throwError, Subject, Observable, firstValueFrom } = await import(
  "rxjs"
);
const { apiErrorMessage } = await load("core/http/api-error");
const { PortalGuard } = await load("core/auth/portal.guard");
const { WorkspaceGuard } = await load("core/auth/workspace.guard");
const { LookupService } = await load("core/services/lookup.service");
const { WorkspaceFacade } = await load("shared/workspace/workspace.facade");
const { InpatientApiService } = await load(
  "features/inpatient/inpatient-api.service",
);
const user = {
  username: "admin",
  role: "ADMIN",
  permissions: fixture.permissions,
  roles: ["ROLE_ADMIN"],
  employeeId: 1,
};
const routeSource = fs.readFileSync(
    "src/app/core/navigation/feature-routes.ts",
    "utf8",
  ),
  routeFile = ts.createSourceFile(
    "routes.ts",
    routeSource,
    ts.ScriptTarget.Latest,
    true,
  );
const routes = {};
function visit(node) {
  if (ts.isObjectLiteralExpression(node)) {
    const props = Object.fromEntries(
      node.properties
        .filter(ts.isPropertyAssignment)
        .map((property) => [
          property.name.getText(routeFile),
          property.initializer,
        ]),
    );
    if (props.path && props.loadChildren) {
      const id = props.path.text;
      const match = props.loadChildren
        .getText(routeFile)
        .match(/features\/([^/]+)\//);
      if (match) routes[id] = match[1];
    }
  }
  ts.forEachChild(node, visit);
}
visit(routeFile);
const workflows = {};
for (const feature of new Set(Object.values(routes))) {
  if (["dashboard", "analytics"].includes(feature)) continue;
  const module = await load(`features/${feature}/${feature}.workflows`);
  workflows[feature] = Object.values(module)[0];
}
function context(workspace, overrides = {}) {
  return {
    active: workspace,
    role: "ADMIN",
    username: "admin",
    employeeId: 1,
    batchAction: "WASTE",
    wardAction: "RETURN",
    selected: null,
    detail: null,
    has: (permission) =>
      !permission || fixture.permissions.includes(permission),
    label: (key) =>
      key
        .replace(/([a-z])([A-Z])/g, "$1 $2")
        .replace(/_/g, " ")
        .replace(/^./, (character) => character.toUpperCase()),
    act() {},
    execute() {},
    showDetail() {},
    download() {},
    ...overrides,
  };
}
const clean = (value) => JSON.parse(JSON.stringify(value));
await check(
  "All 55 workspace definitions retain their labels, API paths, columns and permissions",
  () => assert.deepEqual(WORKSPACES, fixture.workspaces),
);
await check("Every workspace has a lazy route", () =>
  assert.deepEqual(
    Object.keys(routes).sort(),
    WORKSPACES.map((value) => value.id).sort(),
  ),
);
await check(
  "All 53 toolbar task sets match the pre-refactor application",
  () => {
    for (const scenario of fixture.tasks)
      assert.deepEqual(
        clean(
          workflows[routes[scenario.workspace]].tasks(
            context(scenario.workspace),
          ),
        ),
        scenario.expected,
        scenario.workspace,
      );
  },
);
await check(
  "All 1060 row-action scenarios preserve workflow availability and transition labels",
  () => {
    for (const scenario of fixture.actions)
      assert.deepEqual(
        workflows[routes[scenario.workspace]]
          .actions(context(scenario.workspace), scenario.row)
          .map((action) => action.label),
        scenario.expected,
        scenario.workspace + " " + scenario.row.status,
      );
  },
);
await check("Patient menu is restricted to the eight patient workspaces", () =>
  assert.deepEqual(
    visibleWorkspaces({ ...user, role: "PATIENT" }, WORKSPACES).map(
      (value) => value.id,
    ),
    [
      "myappointments",
      "myinvoices",
      "myreports",
      "myprescriptions",
      "mydocuments",
      "myprofile",
      "helpdesk",
      "notifications",
    ],
  ),
);
await check(
  "Custom read-only roles do not receive finance or security workspaces",
  () => {
    const menu = visibleWorkspaces(
      {
        ...user,
        role: "CUSTOM",
        permissions: ["patients.read"],
        employeeId: null,
      },
      WORKSPACES,
    );
    assert(menu.some((value) => value.id === "patients"));
    assert(
      !menu.some((value) => value.id === "billing" || value.id === "roles"),
    );
  },
);
await check("Doctor without patients.write cannot register patients", () =>
  assert.equal(
    workflows.patient.tasks(
      context("patients", {
        role: "DOCTOR",
        has: (permission) => permission === "patients.read",
      }),
    ).length,
    0,
  ),
);
await check(
  "Patient appointment tasks use the owned patient booking endpoint",
  () => {
    const tasks = workflows["patient-portal"].tasks(
      context("myappointments", { role: "PATIENT", has: () => false }),
    );
    assert.equal(tasks[0].path, "patient/book");
    assert(!tasks[0].fields.some((field) => field.key === "patientId"));
  },
);
await check(
  "Patient cells resolve MRN labels without exposing unlabelled numeric identifiers",
  () =>
    assert.equal(
      displayValue({ patient_id: 3 }, "patient_id", {
        patients: [{ id: 3, label: "MRN-00000003 · TEST Patient" }],
      }),
      "MRN-00000003 · TEST Patient",
    ),
);
await check("Unknown patient labels use an explicit patient reference", () =>
  assert.equal(displayValue({ patient_id: 3 }, "patient_id", {}), "Patient #3"),
);
await check(
  "Username validation rejects the email input that previously caused HTTP 400",
  () => {
    const control = new FormControl(
      "test@example.test",
      validatorsFor(field("username", "Username")),
    );
    assert(control.invalid);
    control.setValue("test.patient");
    assert(control.valid);
  },
);
await check(
  "Mobile validation accepts ten digits and rejects malformed numbers",
  () => {
    const control = new FormControl(
      "123",
      validatorsFor(field("mobile", "Mobile")),
    );
    assert(control.invalid);
    control.setValue("9000000101");
    assert(control.valid);
  },
);
await check(
  "Optional reset passwords stay optional but short entered passwords are rejected",
  () => {
    const control = new FormControl(
      null,
      validatorsFor({
        ...field("password", "Password", undefined, "password"),
        required: false,
      }),
    );
    assert(control.valid);
    control.setValue("short");
    assert(control.invalid);
  },
);
await check(
  "Nested purchase lines require item and quantity before submission",
  () => {
    const form = createTaskForm({
      label: "Purchase",
      path: "pharmacy/purchases",
      fields: [],
      lineKey: "lines",
      lines: [
        field("inventoryItemId", "Item", "inventory"),
        field("quantity", "Quantity", undefined, "number"),
      ],
    });
    assert(form.invalid);
    form.controls.lines.at(0).setValue({ inventoryItemId: 1, quantity: 2 });
    assert(form.valid);
  },
);
await check(
  "Dispensing requires exactly one final prescription or verified IPD order",
  () => {
    const task = workflows.pharmacy.tasks(context("dispenses"))[0];
    const form = createTaskForm(task);
    form.controls.fields.patchValue({
      patientId: 3,
      inventoryItemId: 1,
      quantity: 2,
    });
    assert(form.hasError("dispensingSource"));
    form.controls.fields.controls.prescriptionId.setValue(7);
    assert(!form.hasError("dispensingSource"));
    form.controls.fields.controls.medicationOrderId.setValue(8);
    assert(form.hasError("dispensingSource"));
  },
);
await check(
  "Electronic split-payment lines require a reference and verified confirmation",
  () => {
    const task = {
      label: "Pay",
      path: "finance/invoices/1/payments",
      fields: [],
      lineKey: "parts",
      lines: [
        field("method", "Method"),
        field("amount", "Amount", undefined, "number"),
        { ...field("referenceNo", "Reference"), required: false },
        {
          ...field("verified", "Verified", undefined, "checkbox"),
          required: false,
        },
      ],
    };
    const form = createTaskForm(task),
      line = form.controls.lines.at(0);
    line.patchValue({ method: "UPI", amount: 10 });
    assert(line.hasError("verifiedPayment"));
    line.patchValue({ referenceNo: "TEST-1", verified: true });
    assert(form.valid);
  },
);
await check(
  "Cash payment does not require electronic transaction verification",
  () => {
    const form = createTaskForm({
      label: "Pay",
      path: "finance/invoices/1/payments",
      fields: [],
      lineKey: "parts",
      lines: [
        field("method", "Method"),
        field("amount", "Amount", undefined, "number"),
      ],
    });
    form.controls.lines.at(0).setValue({ method: "CASH", amount: 20 });
    assert(form.valid);
  },
);
await check(
  "Optional blank fields are omitted while explicit false is retained",
  () => {
    const task = {
      label: "Account",
      path: "security/accounts/1",
      fields: [
        {
          ...field("enabled", "Enabled", undefined, "checkbox"),
          required: false,
        },
        {
          ...field("password", "Reset", undefined, "password"),
          required: false,
        },
      ],
    };
    assert.deepEqual(
      buildPayload(task, { enabled: false, password: null }, [], [], false),
      { enabled: false },
    );
  },
);
await check(
  "Return credits without cash refunds retain optional payment details",
  () => {
    let captured;
    workflows.billing
      .actions(
        context("creditqueue", { act: (...args) => (captured = args) }),
        { id: 1, status: "PENDING" },
      )[0]
      .run();
    assert(captured[2].every((field) => field.required === false));
  },
);
await check("Managing an account starts with its current enabled state", () => {
  let captured;
  workflows.administration
    .actions(context("accounts", { act: (...args) => (captured = args) }), {
      id: 1,
      enabled: true,
      role: "NURSE",
    })[0]
    .run();
  assert.equal(captured[3].enabled, true);
});
await check(
  "Role permission edits are sent in the existing JSON contract",
  () =>
    assert.deepEqual(
      buildPayload(
        { label: "Role", path: "security/roles/TEST", fields: [] },
        {},
        [],
        ["patients.read"],
        true,
      ),
      { permissions: ["patients.read"] },
    ),
);
await check(
  "Structured backend validation details are shown instead of generic HTTP errors",
  () =>
    assert.equal(
      apiErrorMessage({
        status: 400,
        error: { detail: "Select a final prescription" },
      }),
      "Select a final prescription",
    ),
);
await check("Network failures explain that the backend cannot be reached", () =>
  assert.match(apiErrorMessage({ status: 0 }), /backend is running/),
);
await check(
  "JWT interceptor attaches credentials only to protected same-origin API requests",
  async () => {
    const session = new SessionStore();
    session.setToken("TEST");
    const interceptor = new AuthInterceptor(session);
    let sent;
    const handler = {
      handle: (request) => {
        sent = request;
        return of(new HttpResponse({ status: 200 }));
      },
    };
    await firstValueFrom(
      interceptor.intercept(new HttpRequest("GET", "/api/patients"), handler),
    );
    assert.equal(sent.headers.get("Authorization"), "Bearer TEST");
    await firstValueFrom(
      interceptor.intercept(
        new HttpRequest("GET", "/api/public/slots"),
        handler,
      ),
    );
    assert.equal(sent.headers.get("Authorization"), null);
    await firstValueFrom(
      interceptor.intercept(
        new HttpRequest("GET", "https://example.test/data"),
        handler,
      ),
    );
    assert.equal(sent.headers.get("Authorization"), null);
    session.clear();
  },
);
await check(
  "A current-token 401 clears the session and signals expiration",
  async () => {
    const session = new SessionStore();
    session.setToken("TEST");
    let expired = 0;
    session.expired$.subscribe(() => expired++);
    const interceptor = new AuthInterceptor(session);
    await assert.rejects(
      firstValueFrom(
        interceptor.intercept(new HttpRequest("GET", "/api/patients"), {
          handle: () =>
            throwError(() => new HttpErrorResponse({ status: 401 })),
        }),
      ),
    );
    assert.equal(session.token, "");
    assert.equal(expired, 1);
  },
);
await check(
  "A late 401 from an old token does not sign out a newer session",
  async () => {
    const session = new SessionStore();
    session.setToken("OLD");
    const response = new Subject(),
      interceptor = new AuthInterceptor(session);
    const promise = firstValueFrom(
      interceptor.intercept(new HttpRequest("GET", "/api/patients"), {
        handle: () => response,
      }),
    );
    session.setToken("NEW");
    response.error(new HttpErrorResponse({ status: 401 }));
    await assert.rejects(promise);
    assert.equal(session.token, "NEW");
    session.clear();
  },
);
await check("Session expiration clears tab-memory credentials", async () => {
  const session = new SessionStore();
  session.setToken("TEST");
  session.establish(user, 0);
  await new Promise((resolve) => setTimeout(resolve, 5));
  assert.equal(session.token, "");
  assert.equal(session.user, null);
});
await check(
  "Portal guard preserves the requested URL when sign-in is required",
  () => {
    const router = {
      createUrlTree: (path, extras) => ({ path, extras }),
      parseUrl: (url) => url,
    };
    const guard = new PortalGuard(new SessionStore(), router);
    assert.deepEqual(
      guard.canActivate(
        { paramMap: new Map([["portal", "doctor"]]) },
        { url: "/doctor/visits" },
      ),
      {
        path: ["/login"],
        extras: { queryParams: { returnUrl: "/doctor/visits" } },
      },
    );
  },
);
await check(
  "Portal guard redirects another role URL to the account’s own portal",
  () => {
    const session = new SessionStore();
    session.setToken("TEST");
    session.establish(user, 3600);
    const guard = new PortalGuard(session, { parseUrl: (url) => url });
    assert.equal(
      guard.canActivate(
        { paramMap: new Map([["portal", "doctor"]]) },
        { url: "/doctor" },
      ),
      "/admin",
    );
    session.clear();
  },
);
await check(
  "Workspace guard rejects direct navigation to an unauthorized module",
  () => {
    const session = new SessionStore();
    session.setToken("TEST");
    session.establish(
      { ...user, role: "DOCTOR", permissions: ["patients.read"] },
      3600,
    );
    const guard = new WorkspaceGuard(session, { parseUrl: (url) => url });
    assert.equal(
      guard.canActivate({ data: { workspaceId: "billing" } }),
      "/doctor",
    );
    session.clear();
  },
);
await check(
  "Task lookups fetch current patient choices each time the dialog opens",
  async () => {
    let calls = 0;
    const lookup = new LookupService(
      {
        get: () => {
          calls++;
          return of({
            patients: [
              { id: calls, label: "TEST " + calls, uhid: "MRN-" + calls },
            ],
          });
        },
      },
      { has: () => false },
    );
    const task = {
      label: "Assign",
      path: "care/teams",
      fields: [field("patientId", "Patient", "patients")],
    };
    const first = await firstValueFrom(lookup.forTask(task)),
      second = await firstValueFrom(lookup.forTask(task));
    assert.notEqual(first.patients[0].id, second.patients[0].id);
    assert.match(second.patients[0].label, /MRN-2/);
  },
);
await check(
  "Inpatient API adapter maps selected bed to existing admission payload",
  async () => {
    const api = new InpatientApiService({
      get: () => of([{ id: 7, ward: "GENERAL", bed_no: "G-01" }]),
    });
    assert.deepEqual(
      await firstValueFrom(
        api.prepare(
          { path: "operations/ipd/admit" },
          { patientId: 3, bedId: 7 },
        ),
      ),
      { patientId: 3, ward: "GENERAL", bedNo: "G-01" },
    );
  },
);
await check(
  "Workspace subscriptions are cancelled on route destruction",
  () => {
    let cancelled = 0;
    const data = { load: () => new Observable(() => () => cancelled++) };
    const facade = new WorkspaceFacade(
      {},
      { user, has: () => true },
      { base: () => of({}) },
      { refresh() {} },
      {},
      workflows.patient,
      data,
    );
    facade.initialize(
      WORKSPACES.find((workspace) => workspace.id === "patients"),
    );
    facade.ngOnDestroy();
    assert.equal(cancelled, 1);
  },
);
await check("Refreshing a workspace cancels its older response", () => {
  let cancelled = 0;
  const data = { load: () => new Observable(() => () => cancelled++) };
  const facade = new WorkspaceFacade(
    {},
    { user, has: () => true },
    { base: () => of({}) },
    { refresh() {} },
    {},
    workflows.patient,
    data,
  );
  facade.initialize(
    WORKSPACES.find((workspace) => workspace.id === "patients"),
  );
  facade.refresh();
  assert.equal(cancelled, 1);
  facade.ngOnDestroy();
});
await check(
  "Application root has no workflow, form or HTTP implementation",
  () => {
    const rootSource = fs.readFileSync("src/app/app.component.ts", "utf8");
    assert(
      !/HttpClient|subscribe\(|FormGroup|rowActions|modules\s*[:=]/.test(
        rootSource,
      ),
    );
    assert(rootSource.includes("router-outlet"));
  },
);
await check(
  "Production source contains no explicit any or browser token persistence",
  () => {
    for (const file of walk(source).filter((file) => file.endsWith(".ts"))) {
      const text = fs.readFileSync(file, "utf8"),
        unit = ts.createSourceFile(file, text, ts.ScriptTarget.Latest, true);
      function checkNode(node) {
        assert.notEqual(node.kind, ts.SyntaxKind.AnyKeyword, file);
        ts.forEachChild(node, checkNode);
      }
      checkNode(unit);
      assert(!/localStorage|sessionStorage/.test(text), file);
    }
  },
);
await (
  await import("./clinical-visual.tests.mjs")
).default({ load, check, of, throwError });
fs.mkdirSync("test-results", { recursive: true });
fs.writeFileSync(
  "test-results/frontend-tests.json",
  JSON.stringify(
    {
      passed: results.length,
      workflowTaskSets: fixture.tasks.length,
      rowActionScenarios: fixture.actions.length,
      checks: results,
    },
    null,
    2,
  ) + "\n",
);
console.log(`Frontend tests passed: ${results.length}`);
