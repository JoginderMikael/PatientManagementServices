import { Metric } from "web-vitals";

export type TelemetryEvent = Readonly<{
  category: "application" | "performance";
  name: "unhandled_render_error" | "CLS" | "FID" | "FCP" | "LCP" | "TTFB";
  route: string;
  status: "error" | "ok";
  value?: number;
}>;

type TelemetrySink = (event: TelemetryEvent) => void;
let testSink: TelemetrySink | null = null;

const staticRoutes = new Set([
  "/login",
  "/forbidden",
  "/patient/overview",
  "/patient/appointments",
  "/patient/records",
  "/patient/billing",
  "/patient/access",
  "/staff/work-queue",
  "/staff/patients",
  "/staff/schedule",
  "/staff/clinical-chart",
  "/staff/billing",
  "/staff/insurance",
  "/staff/pharmacy",
  "/staff/inventory",
  "/staff/notifications",
  "/staff/compliance",
  "/staff/audit",
  "/staff/reports",
]);

export function safeRouteTemplate(input: string): string {
  const pathname = input.split(/[?#]/, 1)[0].replace(/\/+$/, "") || "/";
  if (staticRoutes.has(pathname)) return pathname;
  if (
    /^\/staff\/patients\/[^/]+(?:\/(summary|demographics|encounters|medications|labs|notes|vaccinations))?$/.test(
      pathname,
    )
  ) {
    return pathname.replace(
      /^\/staff\/patients\/[^/]+/,
      "/staff/patients/:patientId",
    );
  }
  return "/unknown";
}

function configuredSink(event: TelemetryEvent): void {
  const endpoint = process.env.REACT_APP_TELEMETRY_PATH;
  if (!endpoint || !endpoint.startsWith("/") || endpoint.startsWith("//"))
    return;
  const payload = JSON.stringify(event);
  if (navigator.sendBeacon) {
    navigator.sendBeacon(
      endpoint,
      new Blob([payload], { type: "application/json" }),
    );
    return;
  }
  void fetch(endpoint, {
    method: "POST",
    body: payload,
    headers: { "Content-Type": "application/json" },
    keepalive: true,
    credentials: "same-origin",
  }).catch(() => undefined);
}

function emit(event: TelemetryEvent): void {
  (testSink ?? configuredSink)(Object.freeze(event));
}

export function reportRenderError(): void {
  emit({
    category: "application",
    name: "unhandled_render_error",
    route: safeRouteTemplate(window.location.pathname),
    status: "error",
  });
}

export function reportPerformanceMetric(metric: Metric): void {
  if (!["CLS", "FID", "FCP", "LCP", "TTFB"].includes(metric.name)) return;
  emit({
    category: "performance",
    name: metric.name,
    route: safeRouteTemplate(window.location.pathname),
    status: "ok",
    value: Math.max(0, Math.round(metric.value * 1000) / 1000),
  });
}

export function setTelemetrySinkForTest(sink: TelemetrySink | null): void {
  if (process.env.NODE_ENV !== "test") return;
  testSink = sink;
}
