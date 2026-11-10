/* Additional browser acceptance for the Angular module/form refactor. Synthetic data only. */
const assert = require("node:assert/strict");
module.exports = async function ({
  base,
  admin,
  context,
  login,
  results,
  errors,
}) {
  const suffix = Date.now().toString(36),
    username = "ui.patient." + suffix,
    password = process.env.HMS_DEMO_PASSWORD;
  const { p: patient } = await context();
  await patient.goto(base + "/register");
  let registrationRequests = 0;
  patient.on("request", (request) => {
    if (
      request.method() === "POST" &&
      request.url().endsWith("/api/public/register")
    )
      registrationRequests++;
  });
  await patient.locator("input[name=firstName]").fill("TEST UI " + suffix);
  await patient.locator("input[name=lastName]").fill("Patient");
  await patient.locator("input[name=mobile]").fill("9000000991");
  await patient.locator("input[name=newUsername]").fill("invalid@example.test");
  await patient.locator("input[name=newPassword]").fill(password);
  await patient
    .getByRole("button", { name: "Create patient account", exact: true })
    .click();
  await patient
    .getByText(
      "Enter a valid username. Email addresses containing @ are not accepted.",
      { exact: true },
    )
    .waitFor();
  assert.equal(registrationRequests, 0);
  results.push(
    "Registration rejects an email username locally with a field-level explanation",
  );
  await patient.locator("input[name=newUsername]").fill(username);
  await patient
    .getByRole("button", { name: "Create patient account", exact: true })
    .click();
  await patient.waitForURL("**/login");
  await patient.getByText(/Your MRN is MRN-/).waitFor();
  assert.equal(registrationRequests, 1);
  results.push(
    "Reactive public registration preserves the generated-MRN response",
  );
  await login(patient, username, password);
  await patient
    .getByRole("button", {
      name: "Complete demo registration fee",
      exact: true,
    })
    .click();
  await patient.getByText(/Simulated registration fee completed/).waitFor();
  await patient
    .getByRole("button", { name: "+ Book appointment", exact: true })
    .click();
  const slot = patient.getByLabel("Doctor and available time", { exact: true });
  await slot.locator("option").nth(1).waitFor({ state: "attached" });
  await slot.selectOption({ index: 1 });
  await patient
    .getByLabel("Reason for visit", { exact: true })
    .fill("Synthetic frontend appointment test");
  await patient
    .getByRole("button", { name: "Save & continue →", exact: true })
    .click();
  await patient.locator(".task-drawer").waitFor({ state: "hidden" });
  await patient
    .getByRole("button", { name: "Reschedule", exact: true })
    .waitFor();
  results.push("Reactive appointment form books a patient-owned slot");
  await patient
    .getByRole("button", { name: "Reschedule", exact: true })
    .click();
  const replacement = patient.getByLabel("New available slot", { exact: true });
  await replacement.locator("option").nth(1).waitFor({ state: "attached" });
  await replacement.selectOption({ index: 1 });
  await patient
    .getByRole("button", { name: "Save & continue →", exact: true })
    .click();
  await patient.locator(".task-drawer").waitFor({ state: "hidden" });
  await patient.getByText(/Reschedule appointment completed/).waitFor();
  results.push(
    "Rescheduling reloads available slots and saves through the original endpoint",
  );
  // A second process/tab can add a patient while the Admin session remains open.
  await admin
    .locator("aside nav")
    .getByRole("button", { name: "Care assignments", exact: false })
    .click();
  await admin
    .getByRole("button", { name: "+ Assign care team", exact: true })
    .click();
  await admin.getByLabel("Patient", { exact: true }).waitFor();
  await admin.getByRole("button", { name: "Cancel", exact: true }).click();
  const loginResponse = await admin.request.post(base + "/api/auth/login", {
    data: { username: "admin", password: process.env.HMS_ADMIN_PASSWORD },
  });
  assert(loginResponse.ok());
  const token = (await loginResponse.json()).token;
  const freshName = "Dropdown Refresh " + suffix;
  const created = await admin.request.post(base + "/api/patients", {
    headers: { Authorization: "Bearer " + token },
    data: { firstName: freshName, mobile: "9000000992" },
  });
  assert(created.ok());
  await admin
    .getByRole("button", { name: "+ Assign care team", exact: true })
    .click();
  await admin
    .getByLabel("Patient", { exact: true })
    .locator("option")
    .filter({ hasText: freshName })
    .waitFor({ state: "attached" });
  await admin.getByRole("button", { name: "Cancel", exact: true }).click();
  results.push(
    "Care-assignment choices include a newly created patient without signing out or reloading",
  );
  const { p: pharmacy } = await context();
  await login(pharmacy, "demo.pharmacist", password);
  await pharmacy
    .locator("aside nav")
    .getByRole("button", { name: "Patient dispensing", exact: false })
    .click();
  await pharmacy
    .getByRole("button", { name: "+ Dispense for patient", exact: true })
    .click();
  await pharmacy
    .getByLabel("Patient", { exact: true })
    .locator("option")
    .nth(1)
    .waitFor({ state: "attached" });
  await pharmacy
    .getByLabel("Patient", { exact: true })
    .selectOption({ index: 1 });
  await pharmacy
    .getByLabel("Medicine", { exact: true })
    .locator("option")
    .nth(1)
    .waitFor({ state: "attached" });
  await pharmacy
    .getByLabel("Medicine", { exact: true })
    .selectOption({ index: 1 });
  await pharmacy.getByLabel("Quantity", { exact: true }).fill("2");
  let dispenseRequests = 0;
  pharmacy.on("request", (request) => {
    if (
      request.method() === "POST" &&
      request.url().endsWith("/api/pharmacy/dispenses")
    )
      dispenseRequests++;
  });
  await pharmacy
    .getByRole("button", { name: "Save & continue →", exact: true })
    .click();
  await pharmacy
    .getByText("Select either a final prescription or a verified IPD order.", {
      exact: true,
    })
    .waitFor();
  assert.equal(dispenseRequests, 0);
  await pharmacy.getByRole("button", { name: "Cancel", exact: true }).click();
  results.push(
    "Dispensing explains the missing prescription/IPD source before sending a request",
  );
  const { p: doctor } = await context();
  await doctor.goto(base + "/doctor/billing");
  await doctor.locator("input[name=username]").fill("demo.doctor");
  await doctor.locator("input[name=password]").fill(password);
  await doctor.getByRole("button", { name: "Sign in →", exact: true }).click();
  await doctor.locator(".workspace aside").waitFor();
  await doctor.waitForLoadState("networkidle");
  assert(!doctor.url().endsWith("/billing"));
  assert.equal(
    await doctor
      .locator("aside nav")
      .getByRole("button", { name: "Billing · collections", exact: false })
      .count(),
    0,
  );
  results.push(
    "Direct navigation cannot open a workspace outside the signed-in role permissions",
  );
  await admin
    .locator("aside nav")
    .getByRole("button", { name: "Patient directory", exact: false })
    .click();
  await admin.waitForURL("**/admin/patients");
  await admin
    .locator("aside nav")
    .getByRole("button", { name: "Appointments", exact: false })
    .click();
  await admin.waitForURL("**/admin/appointments");
  await admin.goBack();
  await admin.waitForURL("**/admin/patients");
  await admin
    .getByRole("heading", { name: "Patient directory", exact: true })
    .waitFor();
  results.push("Browser Back restores the routed workspace and header");
  const { p: lab } = await context();
  await login(lab, "demo.lab_technician", password);
  const initialLabResponse = lab.waitForResponse(
    (response) =>
      response.url().endsWith("/api/operations/lab/items") &&
      response.status() === 200,
  );
  await lab
    .locator("aside nav")
    .getByRole("button", { name: "Laboratory", exact: false })
    .click();
  await initialLabResponse;
  await lab.getByTitle("Refresh", { exact: true }).waitFor();
  await lab.waitForLoadState("networkidle");
  const errorOffset = errors.length;
  await lab.route(
    "**/api/operations/lab/items",
    (route) =>
      route.fulfill({
        status: 401,
        contentType: "application/json",
        body: JSON.stringify({ detail: "Synthetic expired session" }),
      }),
    { times: 1 },
  );
  // Trigger the real button handler without retrying a click after its expected redirect.
  await lab
    .getByTitle("Refresh", { exact: true })
    .evaluate((button) => button.click());
  await lab.waitForURL("**/login?reason=sessionExpired");
  await lab
    .getByRole("alert")
    .filter({ hasText: "Your session expired. Sign in again to continue." })
    .waitFor();
  await lab.waitForLoadState("networkidle");
  const expectedErrors = errors.splice(errorOffset);
  assert(
    expectedErrors.every((error) => /401|Failed to load resource/.test(error)),
    JSON.stringify(expectedErrors),
  );
  results.push(
    "A protected-API 401 clears the session and opens sign-in with an explanation",
  );
};
