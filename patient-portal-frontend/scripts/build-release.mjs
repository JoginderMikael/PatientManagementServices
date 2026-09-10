import { spawn } from "node:child_process";

const executable = new URL("../node_modules/react-scripts/bin/react-scripts.js", import.meta.url);
const child = spawn(process.execPath, [executable.pathname.replace(/^\/(.:\/)/, "$1"), "build"], {
  cwd: process.cwd(),
  env: { ...process.env, GENERATE_SOURCEMAP: "false" },
  stdio: "inherit",
});

child.on("error", (error) => {
  console.error(`Release build could not start: ${error.message}`);
  process.exitCode = 1;
});
child.on("exit", (code) => {
  process.exitCode = code ?? 1;
});
