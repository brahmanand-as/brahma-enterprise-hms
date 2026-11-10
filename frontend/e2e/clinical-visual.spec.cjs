/* Actual API and UI acceptance for the clinical visual dashboards; synthetic data only. */
const assert = require("node:assert/strict");
module.exports = async ({ base, admin, context, login, results }) => {
  const response = await admin.request.post(base + "/api/auth/login", {
    data: { username: "admin", password: process.env.HMS_ADMIN_PASSWORD },
  });
  assert(response.ok());
  const { token } = await response.json();
  async function api(method, path, data) {
    const r = await admin.request.fetch(base + "/api/" + path, {
      method,
      headers: { Authorization: "Bearer " + token },
      data,
    });
    assert(r.ok(), path + ": " + r.status() + " " + (await r.text()));
    const body = await r.text();
    return body ? JSON.parse(body) : null;
  }
  const options = await api("GET", "portal/options");
  const doctor = options.doctors[0];
  assert(doctor);
  const one = await api("POST", "patients", {
    firstName: "TEST Visual Aarav",
    lastName: "Rao",
    mobile: "9000000901",
  });
  const two = await api("POST", "patients", {
    firstName: "TEST Visual Meera",
    lastName: "Rao",
    mobile: "9000000902",
  });
  const visit = await api("POST", "workflow/walk-in", {
    patientId: one.id,
    doctorEmployeeId: doctor.id,
    chiefComplaint: "Synthetic dashboard acceptance only",
  });
  await api("POST", "workflow/walk-in", {
    patientId: two.id,
    doctorEmployeeId: doctor.id,
    chiefComplaint: "Synthetic follow-up",
  });
  await api("POST", "care/teams", {
    patientId: one.id,
    username: "demo.nurse",
  });
  await api("POST", "care/teams", {
    patientId: two.id,
    username: "demo.nurse",
  });
  await api("POST", "workflow/encounters/" + visit.id + "/vitals", {
    temperature: 37,
    pulse: 75,
    spo2: 99,
    systolic: 120,
    diastolic: 80,
    weightKg: 65,
  });
  await api("POST", "inpatient/beds", {
    ward: "Visual Care Ward",
    bedNo: "V-01",
    floorName: "First",
    bedType: "GENERAL",
    dailyRate: 500,
  });
  await api("POST", "inpatient/beds", {
    ward: "Visual Care Ward",
    bedNo: "V-02",
    floorName: "First",
    bedType: "ICU",
    dailyRate: 1500,
  });
  await api("POST", "operations/ipd/admit", {
    patientId: one.id,
    ward: "Visual Care Ward",
    bedNo: "V-01",
  });
  await api("POST", "inpatient/recommendations", {
    encounterId: visit.id,
    wardType: "GENERAL",
    reason: "Synthetic monitor test",
  });
  const { p } = await context();
  await login(p, "demo.doctor", process.env.HMS_DEMO_PASSWORD);
  await p.locator("app-doctor-dashboard").waitFor();
  await p
    .getByRole("heading", { name: "Good care starts with a clear view." })
    .waitFor();
  await p
    .locator(".doctor-patient-row")
    .filter({ hasText: "TEST Visual Aarav" })
    .waitFor();
  await p
    .locator(".round-card")
    .filter({ hasText: "TEST Visual Aarav" })
    .waitFor();
  await p.screenshot({
    path: "tools/qa-modern-doctor-desktop.png",
    fullPage: true,
  });
  results.push(
    "Doctor dashboard shows API-backed waiting patients and assigned ward-round cards",
  );
  await p.getByRole("button", { name: "Open OPD board", exact: false }).click();
  await p.locator("app-opd-board").waitFor();
  await p.waitForLoadState("networkidle");
  const card = p
    .locator(".queue-card")
    .filter({ hasText: "TEST Visual Aarav" });
  await card.waitFor();
  await card
    .getByRole("button", { name: "Start consultation", exact: false })
    .waitFor();
  await p.screenshot({
    path: "tools/qa-modern-opd-desktop.png",
    fullPage: true,
  });
  results.push(
    "OPD board resolves MRNs and names and exposes permitted consultation actions",
  );
  await p.getByLabel("Filter OPD doctor").selectOption({ label: doctor.label });
  await p.getByRole("button", { name: "List", exact: true }).click();
  await p.getByRole("cell", { name: /TEST Visual Aarav/ }).waitFor();
  await p.getByRole("button", { name: "Board", exact: true }).click();
  await card
    .getByRole("button", { name: "Start consultation", exact: false })
    .click();
  await p.getByText("Consultation started", { exact: false }).waitFor();
  await p
    .locator(".queue-lane[data-tone=blue]")
    .locator(".queue-card")
    .filter({ hasText: "TEST Visual Aarav" })
    .waitFor();
  results.push(
    "OPD list toggle and doctor filter work; consultation action moves patient to correct lane",
  );
  await p
    .locator(".queue-lane[data-tone=blue]")
    .locator(".queue-card")
    .filter({ hasText: "TEST Visual Aarav" })
    .getByRole("button", { name: "Clinical notes", exact: false })
    .click();
  await p
    .getByLabel("History", { exact: true })
    .fill("Synthetic history for acceptance");
  await p.getByLabel("Examination", { exact: true }).fill("Synthetic exam");
  await p.getByLabel("Diagnosis", { exact: true }).fill("Synthetic QA only");
  await p.getByLabel("Advice", { exact: true }).fill("Synthetic advice");
  await p
    .getByRole("button", { name: "Save & continue", exact: false })
    .click();
  await p.getByText(/Save consultation completed/).waitFor();
  await p
    .locator(".queue-card")
    .filter({ hasText: "TEST Visual Aarav" })
    .getByRole("button", { name: "Sign & send to billing", exact: false })
    .click();
  await p
    .locator(".queue-lane[data-tone=teal]")
    .locator(".queue-card")
    .filter({ hasText: "TEST Visual Aarav" })
    .waitFor();
  results.push(
    "Doctor saves clinical notes and signs consultation from visual board without changing APIs",
  );
  await p
    .locator("aside nav")
    .getByRole("button", { name: "Admissions", exact: false })
    .click();
  await p.locator("app-inpatient-monitor").waitFor();
  await p.waitForLoadState("networkidle");
  await p.getByLabel("Filter inpatient ward").selectOption("Visual Care Ward");
  await p
    .locator(".census-card")
    .filter({ hasText: "TEST Visual Aarav" })
    .waitFor();
  assert.equal(
    await p
      .locator(".census-card")
      .getByRole("button", { name: "Discharge", exact: false })
      .count(),
    0,
  );
  await p.screenshot({
    path: "tools/qa-modern-ipd-desktop.png",
    fullPage: true,
  });
  results.push(
    "IPD monitor shows admitted patient and ward occupancy while keeping doctor discharge controls hidden",
  );
  await p.getByRole("button", { name: "List", exact: true }).click();
  await p.getByRole("cell", { name: /TEST Visual Aarav/ }).waitFor();
  results.push("IPD census list toggle retains patient and admission records");
  const { p: mobile } = await context({ width: 390, height: 844 });
  await login(mobile, "demo.doctor", process.env.HMS_DEMO_PASSWORD);
  for (const view of ["overview", "visits", "admissions"]) {
    if (view !== "overview") {
      await mobile
        .locator("aside nav")
        .getByRole("button", {
          name: view === "visits" ? "OPD · clinical visits" : "Admissions",
          exact: false,
        })
        .click();
      await mobile.waitForLoadState("networkidle");
    }
    const size = await mobile.evaluate(() => ({
      width: document.documentElement.scrollWidth,
      viewport: innerWidth,
    }));
    assert(
      size.width <= size.viewport + 2,
      view + " overflow " + JSON.stringify(size),
    );
    await mobile.screenshot({
      path: "tools/qa-modern-" + view + "-mobile.png",
      fullPage: true,
    });
    results.push("Modern " + view + " fits a 390px doctor viewport");
  }
  const { p: nurse } = await context();
  await login(nurse, "demo.nurse", process.env.HMS_DEMO_PASSWORD);
  await nurse
    .locator("aside nav")
    .getByRole("button", { name: "OPD · clinical visits", exact: false })
    .click();
  await nurse.waitForLoadState("networkidle");
  const nurseCard = nurse
    .locator(".queue-card")
    .filter({ hasText: "TEST Visual Meera" });
  await nurseCard.waitFor();
  await nurseCard
    .getByRole("button", { name: "Record vitals", exact: false })
    .waitFor();
  assert.equal(
    await nurseCard
      .getByRole("button", { name: "Start consultation", exact: false })
      .count(),
    0,
  );
  results.push(
    "Nurse OPD board exposes vitals and hides doctor-only consultation control",
  );
  // Preserve the full role navigation regression suite's existing synthetic state.
};
