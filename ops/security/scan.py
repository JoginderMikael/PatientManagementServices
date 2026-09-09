"""Generate local security evidence and fail on high/critical findings. Requires Docker."""
import pathlib
import os
import json
import shutil
import subprocess
import sys
import tempfile

ROOT = pathlib.Path(__file__).resolve().parents[2]
OUTPUT = ROOT / "integration-tests" / "target" / "security"
CACHE = ROOT / "integration-tests" / "target" / "trivy-cache"
IMAGE = "aquasec/trivy:0.74.0@sha256:62b1e65e8869bc4b4c6aa4fa2b21595256c7c2f6018a9d9ad61caf87187c1969"


def main():
    OUTPUT.mkdir(parents=True, exist_ok=True)
    CACHE.mkdir(parents=True, exist_ok=True)
    # Never present reports from a previous successful run after a tooling failure.
    for name in ("sbom.json", "dependency-findings.json", "findings.json"):
        (OUTPUT / name).unlink(missing_ok=True)
    maven_repository = pathlib.Path(os.environ.get("MAVEN_REPOSITORY", pathlib.Path.home() / ".m2/repository"))
    if not maven_repository.is_dir():
        raise RuntimeError("Run Maven first or set MAVEN_REPOSITORY to its resolved dependency cache")
    # Use Maven's resolved graph, not every version declared in dependencyManagement.
    maven = shutil.which("mvn")
    if not maven:
        raise RuntimeError("Maven is required to generate the resolved dependency SBOM")
    sbom = subprocess.run([maven, "-B", f"-Dmaven.repo.local={maven_repository}",
            "org.cyclonedx:cyclonedx-maven-plugin:2.9.1:makeAggregateBom",
            "-DoutputFormat=json", "-DoutputReactorProjects=false", "-DincludeTestScope=true",
            "-Dcyclonedx.skipAttach=true", f"-DoutputDirectory={OUTPUT}", "-DoutputName=sbom"],
            cwd=ROOT, timeout=1800)
    if sbom.returncode:
        return sbom.returncode
    document = json.loads((OUTPUT / "sbom.json").read_text(encoding="utf-8"))
    if document.get("bomFormat") != "CycloneDX" or not document.get("components"):
        raise RuntimeError("Maven produced an empty or invalid SBOM")
    base = ["docker", "run", "--rm", "-w", "/repo", "-v", f"{ROOT}:/repo:ro", "-v", f"{OUTPUT}:/reports",
            "-v", f"{CACHE}:/root/.cache/trivy", IMAGE]
    dependencies = subprocess.run(base + ["sbom", "--severity", "HIGH,CRITICAL", "--exit-code", "1",
            "--format", "json", "--output", "/reports/dependency-findings.json", "/reports/sbom.json"], timeout=1800)
    source = ["fs", "--offline-scan",
            "--skip-dirs", ".git", "--skip-dirs", ".idea",
            "--skip-dirs", ".vscode", "--skip-dirs", "**/node_modules",
            "--skip-dirs", "infrastructure/cdk.out", "--skip-dirs", "**/target",
            "--skip-dirs", "target", "--skip-files", "*.tmp",
            "--skip-files", ".env", "--skip-files", "*.log"]
    # Scan source even if dependency findings fail the gate, retaining both reports.
    # Prune generated trees before Docker traverses the Windows bind mount.
    with tempfile.TemporaryDirectory(prefix="phase4-source-", dir=OUTPUT.parent) as temporary:
        staged = pathlib.Path(temporary) / "source"
        shutil.copytree(ROOT, staged, symlinks=True, ignore=shutil.ignore_patterns(
            ".git", ".idea", ".vscode", "target", "node_modules", "cdk.out", "__pycache__",
            "*.tmp", "*.log", ".env"))
        source_base = [f"{staged}:/repo:ro" if argument == f"{ROOT}:/repo:ro" else argument for argument in base]
        result = subprocess.run(source_base + source + ["--scanners", "secret,misconfig", "--severity", "HIGH,CRITICAL",
                "--exit-code", "1", "--format", "json", "--output", "/reports/findings.json", "."], timeout=1800)
    print("Security reports:", OUTPUT)
    return dependencies.returncode or result.returncode


if __name__ == "__main__":
    sys.exit(main())
