import assert from "node:assert/strict";
export default async function ({ load, check, of, throwError }) {
  const utils = await load("shared/clinical/care-view.utils");
  const now = Date.parse("2026-10-30T10:00:00+05:30");
  await check(
    "Clinical dates interpret hospital wall time and explicit offsets consistently",
    () => {
      assert.equal(utils.hospitalTime("2026-10-30T10:00:00"), now);
      assert.equal(utils.hospitalTime("2026-10-30T04:30:00Z"), now);
      assert.equal(utils.hospitalDay("2026-10-29T20:00:00Z"), "2026-10-30");
      assert.equal(utils.hospitalDay("bad date"), "");
    },
  );
  await check(
    "Waiting clocks clamp future timestamps and retain missing-time state",
    () => {
      assert.equal(utils.elapsedMinutes("2026-10-30T09:30:00", now), 30);
      assert.equal(utils.elapsedMinutes("2026-10-30T10:30:00", now), 0);
      assert.equal(utils.elapsedMinutes(null, now), null);
    },
  );
  const visits = [
    {
      id: 2,
      patientId: 3,
      startedAt: "2026-10-30T09:45:00",
      doctorName: "Dr B",
      status: "WAITING",
    },
    {
      id: 1,
      patientId: 1,
      startedAt: "2026-10-30T09:00:00",
      doctorName: "Dr A",
      status: "CHECKED_IN",
    },
    {
      id: 3,
      startedAt: "2026-10-30T09:00:00",
      doctorName: "Dr A",
      status: "IN_CONSULTATION",
    },
    { id: 4, startedAt: "2026-10-29T09:00:00", status: "COMPLETED" },
  ];
  await check(
    "OPD queue filters hospital date and doctor, ordered by oldest check-in",
    () => {
      assert.deepEqual(
        utils.queueRows(visits, "2026-10-30", "Dr A").map((r) => r.id),
        [1, 3],
      );
      assert.equal(utils.queueRows(visits, "", "").length, 4);
    },
  );
  await check(
    "Average current wait excludes consulting, completed and missing timestamps",
    () => {
      assert.equal(
        utils.waitingAverage([...visits, { status: "WAITING" }], now),
        38,
      );
      assert.equal(utils.waitingAverage([{ status: "COMPLETED" }], now), null);
    },
  );
  const { OpdBoardComponent } = await load(
    "features/clinical/opd-board.component",
  );
  await check(
    "OPD board retains other statuses and shows scoped patient names",
    () => {
      const board = new OpdBoardComponent();
      board.day = "";
      board.context = {
        filtered: [...visits, { status: "CANCELLED" }],
        rows: visits,
        options: { patients: [{ id: 3, label: "MRN-3 · TEST Aarav" }] },
      };
      assert.equal(board.lane("WAITING").length, 2);
      assert.equal(board.other.length, 1);
      assert.equal(board.name(visits[0]), "MRN-3 · TEST Aarav");
      assert.deepEqual(board.doctors, ["Dr A", "Dr B"]);
    },
  );
  const { InpatientMonitorComponent } = await load(
    "features/inpatient/inpatient-monitor.component",
  );
  const admission = {
    id: 8,
    patientId: 3,
    status: "ADMITTED",
    ward: "ICU",
    admittedAt: "2026-10-30T09:00:00",
  };
  const context = {
    rows: [admission, { id: 9, status: "DISCHARGED", ward: "General" }],
    filtered: [admission],
    options: {},
  };
  await check(
    "IPD monitor computes ward capacity and occupancy from returned bed states",
    () => {
      const view = new InpatientMonitorComponent({}, {}, {});
      view.context = context;
      assert.equal(view.countBed("AVAILABLE"), null);
      view.beds = [
        { ward: "ICU", status: "OCCUPIED" },
        { ward: "ICU", status: "AVAILABLE" },
        { ward: "General", status: "CLEANING" },
      ];
      assert.equal(view.percent("ICU"), 50);
      view.ward = "ICU";
      assert.equal(view.countBed("AVAILABLE"), 1);
      assert.equal(view.active.length, 1);
      assert.equal(view.filtered.length, 1);
    },
  );
  await check(
    "IPD medication reminders use actual ORDERED eMAR status, exclude future and given doses",
    () => {
      const view = new InpatientMonitorComponent({}, {}, {});
      view.context = context;
      const yesterday = new Date(Date.now() - 86400000).toISOString(),
        tomorrow = new Date(Date.now() + 86400000).toISOString();
      view.medications = [
        { admission_id: 8, status: "ORDERED", scheduled_at: yesterday },
        { admission_id: 8, status: "GIVEN", scheduled_at: yesterday },
        { admission_id: 8, status: "ORDERED", scheduled_at: tomorrow },
      ];
      assert.equal(view.due, 1);
      assert.equal(view.dueFor(admission), 1);
      view.medications = null;
      assert.equal(view.due, null);
    },
  );
  await check(
    "IPD auxiliary loading failure remains unavailable with a retry explanation",
    () => {
      const view = new InpatientMonitorComponent(
        { get: () => throwError(() => new Error("offline")) },
        {},
        {},
      );
      view.context = context;
      view.load();
      assert.equal(view.beds, null);
      assert.equal(view.pending, null);
      assert.equal(view.due, null);
      assert.match(view.warning, /could not be loaded/);
      assert.equal(view.loading, false);
      view.ngOnDestroy();
    },
  );
  const { DoctorDashboardComponent } = await load(
    "features/dashboard/doctor-dashboard.component",
  );
  await check(
    "Doctor dashboard requests only APIs permitted by the current role",
    () => {
      const paths = [];
      const doctor = new DoctorDashboardComponent(
        { has: (p) => p === "clinical.read" },
        {
          get: (path) => {
            paths.push(path);
            return of(visits);
          },
        },
        { base: () => of({}) },
        {},
      );
      doctor.refresh();
      assert.deepEqual(paths, ["encounters"]);
      assert.equal(doctor.visits.length, 4);
      assert.deepEqual(doctor.appointments, []);
      doctor.ngOnDestroy();
    },
  );
  await check(
    "Doctor dashboard limits current-day queue and separates active admissions",
    () => {
      const doctor = new DoctorDashboardComponent({}, {}, {}, {});
      const today = utils.hospitalDay();
      doctor.visits = [
        { startedAt: today + "T09:00:00", status: "WAITING" },
        { startedAt: "2020-01-01T09:00:00", status: "WAITING" },
        { startedAt: today + "T09:00:00", status: "IN_CONSULTATION" },
      ];
      doctor.admissions = context.rows;
      assert.equal(doctor.waiting.length, 1);
      assert.equal(doctor.consulting.length, 1);
      assert.equal(doctor.inpatients.length, 1);
      doctor.ngOnDestroy();
    },
  );
  await check(
    "Doctor partial-load failures mark headline counts unavailable",
    () => {
      const doctor = new DoctorDashboardComponent(
        { has: () => true },
        { get: () => throwError(() => new Error("offline")) },
        { base: () => of({}) },
        {},
      );
      doctor.refresh();
      assert.equal(doctor.metric("Clinical queue", 0), "—");
      assert(doctor.warnings.includes("Clinical queue unavailable"));
      doctor.ngOnDestroy();
    },
  );
}
