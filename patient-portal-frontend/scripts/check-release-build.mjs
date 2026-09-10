import { promises as fs } from "node:fs";
import path from "node:path";
import { gzipSync } from "node:zlib";

const build = path.resolve("build");
const budgets = { ".js": 190 * 1024, ".css": 32 * 1024 };

async function filesBelow(directory) {
  const entries = await fs.readdir(directory, { withFileTypes: true });
  return (await Promise.all(entries.map((entry) => {
    const target = path.join(directory, entry.name);
    return entry.isDirectory() ? filesBelow(target) : [target];
  }))).flat();
}

const files = await filesBelow(build);
const maps = files.filter((file) => file.endsWith(".map"));
if (maps.length) throw new Error(`Production source maps are forbidden: ${maps.join(", ")}`);

const totals = { ".js": 0, ".css": 0 };
for (const file of files) {
  const extension = path.extname(file);
  if (extension in totals) totals[extension] += gzipSync(await fs.readFile(file)).length;
}

for (const [extension, limit] of Object.entries(budgets)) {
  if (totals[extension] > limit) {
    throw new Error(`${extension} gzip total ${totals[extension]} exceeds ${limit} byte budget`);
  }
}

const manifest = JSON.parse(await fs.readFile(path.join(build, "asset-manifest.json"), "utf8"));
for (const asset of Object.values(manifest.files ?? {})) {
  if ((asset.endsWith(".js") || asset.endsWith(".css")) && !/\.[a-f0-9]{8,}\./i.test(asset)) {
    throw new Error(`Executable asset is not content-hashed: ${asset}`);
  }
}

console.log(`Release build verified: JS ${totals[".js"]} B gzip / CSS ${totals[".css"]} B gzip; no source maps.`);
