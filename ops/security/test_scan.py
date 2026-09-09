import json
import pathlib
import subprocess
import tempfile
import unittest
from unittest.mock import patch

import scan


class SecurityGateTest(unittest.TestCase):
    def run_scan(self, directory, dependency_status=0, source_status=0, emit_sbom=True):
        output = pathlib.Path(directory) / "reports"
        calls = []

        def execute(command, **kwargs):
            calls.append(command)
            if len(calls) == 1:
                if emit_sbom:
                    (output / "sbom.json").write_text(json.dumps({
                        "bomFormat": "CycloneDX", "components": [{"name": "synthetic"}]
                    }), encoding="utf-8")
                return subprocess.CompletedProcess(command, 0)
            return subprocess.CompletedProcess(command, dependency_status if len(calls) == 2 else source_status)

        with patch.object(scan, "OUTPUT", output), patch.object(scan, "CACHE", pathlib.Path(directory) / "cache"), \
                patch.dict(scan.os.environ, {"MAVEN_REPOSITORY": directory}), \
                patch.object(scan.shutil, "which", return_value="mvn"), \
                patch.object(scan.shutil, "copytree"), \
                patch.object(scan.subprocess, "run", side_effect=execute):
            return scan.main(), calls

    def test_dependency_findings_do_not_prevent_source_evidence(self):
        with tempfile.TemporaryDirectory() as directory:
            status, calls = self.run_scan(directory, dependency_status=1)
        self.assertEqual(1, status)
        self.assertEqual(3, len(calls))
        self.assertIn("sbom", calls[1])
        self.assertIn("fs", calls[2])

    def test_source_findings_fail_even_with_clean_dependencies(self):
        with tempfile.TemporaryDirectory() as directory:
            status, _ = self.run_scan(directory, source_status=1)
        self.assertEqual(1, status)

    def test_missing_new_sbom_cannot_reuse_stale_evidence(self):
        with tempfile.TemporaryDirectory() as directory:
            output = pathlib.Path(directory) / "reports"
            output.mkdir()
            (output / "sbom.json").write_text('{"bomFormat":"CycloneDX","components":[{}]}')
            with self.assertRaises(FileNotFoundError):
                self.run_scan(directory, emit_sbom=False)


if __name__ == "__main__":
    unittest.main()
