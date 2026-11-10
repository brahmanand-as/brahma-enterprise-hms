/* Run against the local UI + fresh synthetic API. Requires Playwright. */
const fs = require("node:fs");
fs.mkdirSync("tools", { recursive: true });
const { chromium } = require(process.env.PLAYWRIGHT_MODULE || "playwright");
(async () => {
  const launch = { headless: true };
  if (process.env.HMS_CHROMIUM_PATH) {
    launch.executablePath = process.env.HMS_CHROMIUM_PATH;
    launch.args = [
      "--no-sandbox",
      "--disable-dev-shm-usage",
      "--single-process",
      "--no-zygote",
      "--disable-gpu",
      "--disable-software-rasterizer",
      "--disable-webgl",
      "--use-gl=disabled",
    ];
  }
  const browser = await chromium.launch(launch),
    results = [],
    errors = [];
  const base = process.env.HMS_UI_URL || "http://127.0.0.1:4200";
  async function context(viewport = { width: 1440, height: 1040 }) {
    const ctx = await browser.newContext({ viewport });
    const p = await ctx.newPage();
    p.on("pageerror", (e) => errors.push(e.message));
    p.on("console", (m) => {
      if (m.type() === "error") errors.push(m.text());
    });
    p.on("response", (r) => {
      if (r.url().includes("/api/") && r.status() >= 400)
        errors.push(r.status() + " " + r.url());
    });
    return { ctx, p };
  }
  async function login(p, user, password) {
    await p.goto(base + "/login");
    await p.locator("input[name=username]").fill(user);
    await p.locator("input[name=password]").fill(password);
    await p.getByRole("button", { name: "Sign in →", exact: true }).click();
    await p.locator(".workspace aside").waitFor();
    await p.waitForLoadState("networkidle");
  }
  const { ctx: adminContext, p: admin } = await context();
  await admin.goto(base);
  await admin
    .getByRole("heading", { name: "Better care, every step of the way." })
    .waitFor();
  await admin.screenshot({ path: "tools/qa-home-desktop.png", fullPage: true });
  results.push("Desktop hospital home renders");
  await login(admin, "admin", process.env.HMS_ADMIN_PASSWORD);
  await admin.screenshot({
    path: "tools/qa-admin-overview.png",
    fullPage: true,
  });
  if (process.env.HMS_UI_FOCUS === "clinical") {
    await require("../frontend/e2e/clinical-visual.spec.cjs")({
      base,
      admin,
      context,
      login,
      results,
      errors,
    });
    await require("../frontend/e2e/portal-refactor.spec.cjs")({
      base,
      admin,
      context,
      login,
      results,
      errors,
    });
    if (errors.length) throw new Error(JSON.stringify(errors));
    fs.writeFileSync(
      "clinical-browser-result.json",
      JSON.stringify({ passed: results.length, checks: results }, null, 2),
    );
    await browser.close();
    console.log("Focused clinical browser checks passed:", results.length);
    return;
  }
  await admin
    .locator("aside nav")
    .getByRole("button", { name: "Patient directory", exact: false })
    .click();
  await admin
    .getByRole("button", { name: "Register patient", exact: false })
    .click();
  await admin
    .getByLabel("First name", { exact: true })
    .fill("Browser synthetic");
  await admin.getByLabel("Last name", { exact: true }).fill("QA");
  await admin
    .getByLabel("Gender", { exact: true })
    .selectOption({ label: "FEMALE" });
  await admin.getByLabel("Date of birth", { exact: true }).fill("2000-01-01");
  await admin.getByLabel("Mobile", { exact: true }).fill("0000000009");
  await admin
    .getByLabel("Address", { exact: true })
    .fill("Synthetic browser test");
  await admin.getByRole("button", { name: "Save & continue →" }).click();
  await admin.locator(".task-drawer").waitFor({ state: "hidden" });
  await admin
    .getByRole("cell", { name: "Browser synthetic", exact: true })
    .waitFor();
  results.push(
    "Admin registers patient through labeled form; generated MRN appears",
  );
  const adminMenus = await admin.locator("aside nav button").allTextContents();
  for (const text of adminMenus) {
    const label = text
      .trim()
      .replace(/^[^A-Za-z]+/, "")
      .replace(/\s+\d+$/, "")
      .trim();
    if (!label) continue;
    const button = admin
      .locator("aside nav button")
      .filter({ hasText: label })
      .first();
    await button.click();
    await admin.waitForLoadState("networkidle");
    if (await admin.locator(".notice.error").count())
      throw new Error(
        "Admin workspace " +
          label +
          ": " +
          (await admin.locator(".notice.error").textContent()),
      );
    results.push("Admin navigation: " + label);
  }
  await admin
    .locator("aside nav")
    .getByRole("button", { name: "Beds · IPD · ICU", exact: false })
    .click();
  await admin.screenshot({ path: "tools/qa-bed-board.png", fullPage: true });
  for (const role of [
    "doctor",
    "nurse",
    "receptionist",
    "ipd",
    "lab_technician",
    "lab_supervisor",
    "pathologist",
    "radiologist",
    "pharmacist",
    "billing",
    "insurance",
    "hr",
    "helpdesk",
    "patient",
  ]) {
    const { ctx, p } = await context();
    await login(p, "demo." + role, process.env.HMS_DEMO_PASSWORD);
    if (role === "doctor") {
      await p
        .locator("aside nav")
        .getByRole("button", { name: "Patient directory", exact: false })
        .click();
      if (
        await p
          .getByRole("button", { name: "Register patient", exact: false })
          .count()
      )
        throw new Error("Doctor has patient registration control");
      results.push("Doctor patient demographics are read-only");
      await p.screenshot({ path: "tools/qa-doctor.png", fullPage: true });
    }
    const buttons = await p.locator("aside nav button").allTextContents();
    for (const text of buttons) {
      const label = text
        .trim()
        .replace(/^[^A-Za-z]+/, "")
        .replace(/\s+\d+$/, "")
        .trim();
      if (!label) continue;
      await p
        .locator("aside nav button")
        .filter({ hasText: label })
        .first()
        .click();
      await p.waitForLoadState("networkidle");
      if (await p.locator(".notice.error").count())
        throw new Error(
          role +
            " workspace " +
            label +
            ": " +
            (await p.locator(".notice.error").textContent()),
        );
    }
    results.push("Role navigation and authorized data loads: " + role);
  }
  const { ctx: mobileContext, p: mobile } = await context({
    width: 390,
    height: 844,
  });
  await login(mobile, "demo.patient", process.env.HMS_DEMO_PASSWORD);
  const dimensions = await mobile.evaluate(() => ({
    width: document.documentElement.scrollWidth,
    viewport: innerWidth,
  }));
  if (dimensions.width > dimensions.viewport + 2)
    throw new Error("Mobile page overflow " + JSON.stringify(dimensions));
  await mobile.screenshot({
    path: "tools/qa-patient-mobile.png",
    fullPage: true,
  });
  results.push("Patient mobile portal fits a 390px viewport");
  await require("../frontend/e2e/clinical-visual.spec.cjs")({
    base,
    admin,
    context,
    login,
    results,
    errors,
  });
  await require("../frontend/e2e/portal-refactor.spec.cjs")({
    base,
    admin,
    context,
    login,
    results,
    errors,
  });
  if (errors.length)
    throw new Error("Browser/HTTP errors: " + JSON.stringify(errors));
  fs.writeFileSync(
    "browser-smoke-result.json",
    JSON.stringify({ passed: results.length, checks: results }, null, 2),
  );
  await browser.close();
  console.log("Browser checks passed:", results.length);
})().catch((e) => {
  console.error(e);
  process.exit(1);
});
