import { createHash } from "node:crypto";
import { promises as fs } from "node:fs";
import path from "node:path";

const baseUrl = (process.env.API_BASE_URL ?? "http://localhost:4004").replace(/\/$/, "");
const token = process.env.API_TOKEN;
const manifestPath = path.resolve("contracts/frontend-api-contract.json");
const manifest = JSON.parse(await fs.readFile(manifestPath, "utf8"));

function canonicalPath(value) {
  return value.replace(/\{[^}]+\}/g, "{param}");
}

function stable(value) {
  if (Array.isArray(value)) return value.map(stable);
  if (value && typeof value === "object") {
    return Object.fromEntries(Object.keys(value).sort().map((key) => [key, stable(value[key])]));
  }
  return value;
}

function operationFingerprint(operation) {
  const contract = {
    parameters: operation.parameters ?? [],
    requestBody: operation.requestBody ?? null,
    responses: operation.responses ?? {},
  };
  return createHash("sha256").update(JSON.stringify(stable(contract))).digest("hex").slice(0, 16);
}

const headers = token ? { Authorization: `Bearer ${token}` } : {};
const evidence = [];
let missing = 0;

for (const service of manifest.services) {
  const response = await fetch(`${baseUrl}${service.document}`, { headers });
  if (!response.ok) throw new Error(`${service.name} OpenAPI export returned HTTP ${response.status}`);
  const document = await response.json();
  for (const required of service.operations) {
    const [method, requiredPath] = required.split(" ");
    const actualPath = Object.keys(document.paths ?? {}).find((candidate) => canonicalPath(candidate) === canonicalPath(requiredPath));
    const operation = actualPath ? document.paths[actualPath]?.[method.toLowerCase()] : undefined;
    if (!operation) {
      console.error(`MISSING ${service.name}: ${required}`);
      missing += 1;
      continue;
    }
    evidence.push({ service: service.name, operation: required, fingerprint: operationFingerprint(operation) });
  }
}

if (missing) throw new Error(`${missing} frontend-required API operation(s) are absent from the deployed OpenAPI revision`);
console.log(`API contract verified at ${baseUrl}: ${evidence.length} required operations across ${manifest.services.length} services.`);
if (process.argv.includes("--print-evidence")) console.log(JSON.stringify(evidence, null, 2));
