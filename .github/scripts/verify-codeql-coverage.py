from pathlib import Path
import json
import sys
from urllib.parse import unquote

language = sys.argv[1]
report_directory = Path(sys.argv[2])
reports = list(report_directory.glob("*.sarif"))
assert reports, "CodeQL did not produce SARIF"
metrics = []
locations = set()
findings = []

def collect_locations(value):
    if isinstance(value, dict):
        location = value.get("artifactLocation", {})
        if isinstance(location, dict) and isinstance(location.get("uri"), str):
            locations.add(unquote(location["uri"]).replace("\\", "/"))
        for child in value.values():
            collect_locations(child)
    elif isinstance(value, list):
        for child in value:
            collect_locations(child)

for path in reports:
    assert path.stat().st_size < 50_000_000, str(path)
    data = json.loads(path.read_text())
    for run in data.get("runs", []):
        for invocation in run.get("invocations", []):
            for notification in invocation.get("toolExecutionNotifications", []):
                descriptor = notification.get("descriptor", {}).get("id", "")
                if descriptor.endswith("/diagnostics/successfully-extracted-files"):
                    collect_locations(notification)
        metrics.extend(run.get("properties", {}).get("metricResults", []))
        findings.extend(run.get("results", []))

if language == "java-kotlin":
    expected = [
        "scheme-reporter-gradle-plugin/src/main/kotlin/org/tinywind/SchemeReporterPlugin.kt",
        "scheme-reporter-gradle-plugin/src/main/kotlin/org/tinywind/SchemeReporterTask.kt",
        "scheme-reporter-gradle-plugin/src/main/kotlin/org/tinywind/SchemeReporterExtension.kt",
        "scheme-reporter-gradle-plugin/src/test/kotlin/org/tinywind/SchemeReporterPluginFunctionalTest.kt",
    ]
    kotlin_metrics = [m for m in metrics if m.get("ruleId", m.get("rule", {}).get("id")) == "java/summary/lines-of-code-kotlin"]
    assert any(float(m.get("value", 0)) > 0 for m in kotlin_metrics), f"No Kotlin LOC: {metrics}"
else:
    expected = [".github/workflows/dependency-compatibility.yml", ".github/workflows/codeql-source.yml"]
missing = [name for name in expected if not any(uri.endswith(name) for uri in locations)]
summary = {"language": language, "metrics": metrics, "expected_source_paths": expected, "missing_source_paths": missing, "result_count": len(findings)}
(report_directory / ("coverage-" + language + ".json")).write_text(json.dumps(summary, indent=2) + "\n")
print(json.dumps(summary, indent=2))
assert not missing, f"Source paths missing from raw SARIF: {missing}"
assert not findings, f"CodeQL reported {len(findings)} findings"
