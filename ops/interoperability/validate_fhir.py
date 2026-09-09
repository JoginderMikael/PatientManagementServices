"""Validate only synthetic response fixtures emitted by Phase4FhirTest."""
import hashlib
import json
import pathlib
import subprocess
import urllib.request

ROOT = pathlib.Path(__file__).resolve().parents[2]
JAR = ROOT / "integration-tests/target/tools/validator_cli.jar"
VERSION = "6.10.4"
SHA256 = "1106b9d58f9e363e47bea7c4fc065841e5fc91fe9d062775c3bfdd212bd653cc"


def main():
    fixtures = ROOT / "patient-service/target/fhir-conformance"
    for name in ("patient", "capability", "outcome"):
        if not (fixtures / (name + ".json")).is_file():
            raise RuntimeError("Run Phase4FhirTest before conformance validation")
    JAR.parent.mkdir(parents=True, exist_ok=True)
    if not JAR.exists():
        urllib.request.urlretrieve(
            f"https://github.com/hapifhir/org.hl7.fhir.core/releases/download/{VERSION}/validator_cli.jar", JAR)
    with JAR.open("rb") as source:
        if hashlib.file_digest(source, "sha256").hexdigest() != SHA256:
            raise RuntimeError("FHIR validator checksum mismatch")
    output = ROOT / "integration-tests/target/fhir-validation.json"
    subprocess.run(["java", "-jar", str(JAR), str(fixtures), "-version", "4.0.1", "-tx", "n/a",
                    "-output", str(output)], check=True, timeout=900)
    result = json.loads(output.read_text(encoding="utf-8"))
    outcomes = [entry["resource"] for entry in result.get("entry", [])] if result.get("resourceType") == "Bundle" else [result]
    if len(outcomes) < 3 or any(issue.get("severity") in ("error", "fatal") for outcome in outcomes for issue in outcome.get("issue", [])):
        raise RuntimeError("FHIR conformance validation failed; inspect " + str(output))


if __name__ == "__main__":
    main()
