import {
  reportRenderError,
  safeRouteTemplate,
  setTelemetrySinkForTest,
  TelemetryEvent,
} from "./safeTelemetry";

describe("privacy-safe frontend telemetry", () => {
  afterEach(() => setTelemetrySinkForTest(null));

  test("replaces patient identifiers and drops query values", () => {
    expect(
      safeRouteTemplate(
        "/staff/patients/10000000-0000-4000-8000-000000000099/notes?email=patient@example.test",
      ),
    ).toBe("/staff/patients/:patientId/notes");
    expect(
      safeRouteTemplate("/not-a-release-route/secret-value?token=secret"),
    ).toBe("/unknown");
  });

  test("render error events contain only allowlisted technical fields", () => {
    const events: TelemetryEvent[] = [];
    setTelemetrySinkForTest((event) => events.push(event));
    window.history.pushState(
      {},
      "",
      "/staff/patients/private-patient-id/summary?token=private-token",
    );
    reportRenderError();
    expect(events).toEqual([
      {
        category: "application",
        name: "unhandled_render_error",
        route: "/staff/patients/:patientId/summary",
        status: "error",
      },
    ]);
    expect(JSON.stringify(events)).not.toMatch(/private|token|patient-id/i);
  });
});
