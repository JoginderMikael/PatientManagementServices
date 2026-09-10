import { spawn } from "node:child_process";

const port = 18080 + Math.floor(Math.random() * 1000);
const origin = `http://127.0.0.1:${port}`;
const child = spawn(process.execPath, ["container/server.mjs"], {
  cwd: process.cwd(),
  env: { ...process.env, PORT: String(port), BUILD_ROOT: "build", API_GATEWAY_URL: "http://127.0.0.1:1" },
  stdio: "ignore",
});

const requiredHeaders = [
  "content-security-policy",
  "cross-origin-opener-policy",
  "cross-origin-resource-policy",
  "permissions-policy",
  "referrer-policy",
  "x-content-type-options",
  "x-frame-options",
];

const probe = (path) => fetch(`${origin}${path}`, {
  headers: { Connection: "close" },
  signal: AbortSignal.timeout(2_000),
});

async function waitForServer() {
  for (let attempt = 0; attempt < 40; attempt += 1) {
    try {
      const response = await probe("/healthz");
      await response.text();
      if (response.ok) return;
    } catch {}
    await new Promise((resolve) => setTimeout(resolve, 100));
  }
  throw new Error("Frontend runtime did not become healthy");
}

try {
  await waitForServer();
  const index = await probe("/patient/overview");
  if (index.headers.get("cache-control") !== "no-store") throw new Error("SPA HTML must use Cache-Control: no-store");
  for (const header of requiredHeaders) if (!index.headers.has(header)) throw new Error(`Missing security header: ${header}`);

  const html = await index.text();
  const assetPath = html.match(/(?:src|href)="([^"]+\.(?:js|css))"/)?.[1];
  if (!assetPath) throw new Error("No built JS/CSS asset found in index.html");
  const asset = await fetch(new URL(assetPath, origin), { headers: { Connection: "close" }, signal: AbortSignal.timeout(2_000) });
  if (!asset.headers.get("cache-control")?.includes("immutable")) throw new Error("Hashed assets must be immutable");
  await asset.arrayBuffer();

  const missingMap = await probe("/static/js/not-present.js.map");
  if (missingMap.status !== 404 || missingMap.headers.get("cache-control") !== "no-store") {
    throw new Error("Missing source-map paths must return a non-cacheable 404");
  }
  await missingMap.text();
  console.log("Runtime verified: CSP/security headers, HTML no-store, immutable hashed assets, source-map 404.");
} finally {
  child.kill("SIGTERM");
  child.unref();
}
