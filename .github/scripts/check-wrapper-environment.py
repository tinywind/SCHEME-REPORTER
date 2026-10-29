from pathlib import Path
import json
import os
import subprocess
import sys

project = Path(sys.argv[1]).resolve()
work = project.parent / ".tmp" / "wrapper-environment-check"
work.mkdir(parents=True, exist_ok=True)
runtime = work / "runtime"
runtime.mkdir(exist_ok=True)
filter_pattern = "!org.jetbrains.kotlin.com.google.common.collect.CompactHashMap;!org.jetbrains.kotlin.com.google.common.collect.CompactHashSet;!org.jetbrains.kotlin.com.google.common.collect.MapMakerInternalMap$*;*"
strict_filter = "maxdepth=8;maxarray=1024;!example.Forbidden;*"
option_names = ["JAVA_TOOL_OPTIONS", "JDK_JAVA_OPTIONS", "_JAVA_OPTIONS", "JAVA_OPTS", "GRADLE_OPTS"]
base = dict(os.environ)
for name in option_names + ["SCHEME_REPORTER_SERIAL_FILTER", "SCHEME_REPORTER_GUARDED_JAVA_TOOL_OPTIONS"]:
    base.pop(name, None)
retained = f'-Djava.io.tmpdir="{runtime}" -DschemeReporter.retained="kept & value!"'
init_script = work / "assert-environment.gradle"
init_script.write_text("assert System.getProperty('schemeReporter.retained') == 'kept & value!'\nassert java.io.ObjectInputFilter.Config.getSerialFilter() != null\nprintln('WRAPPER_POLICY_PRESERVED')\n")
receipts = []

def run(name, env, protected):
    args = ["help", "--quiet", "--init-script", str(init_script)] if protected else ["--version"]
    wrapper = project / ("gradlew.bat" if os.name == "nt" else "gradlew")
    command = [str(wrapper), *args]
    if os.name == "nt":
        command = ["cmd.exe", "/d", "/e:on", "/v:on", "/c", subprocess.list2cmdline(command)]
    log = work / (name + ".log")
    with log.open("w") as output:
        result = subprocess.run(command, cwd=project, env=env, stdout=output, stderr=subprocess.STDOUT, timeout=240)
    assert log.stat().st_size < 2_000_000, str(log)
    text = log.read_text(errors="replace")
    receipts.append({"name": name, "exit": result.returncode, "log": str(log)})
    (work / "receipt.json").write_text(json.dumps(receipts, indent=2) + "\n")
    if protected:
        assert result.returncode == 0 and "WRAPPER_POLICY_PRESERVED" in text, f"{name}: {log}"
    else:
        assert result.returncode != 0 and "preconfigured serialization filter" in text.lower(), f"{name}: {log}"
    print(name, result.returncode, flush=True)

run("retained-option", dict(base, JAVA_TOOL_OPTIONS=retained), True)
guarded = retained + " -Djdk.serialFilter=" + filter_pattern
inherited = dict(base, JAVA_TOOL_OPTIONS=guarded, SCHEME_REPORTER_SERIAL_FILTER=filter_pattern, SCHEME_REPORTER_GUARDED_JAVA_TOOL_OPTIONS=guarded)
run("inherited-owned-guard", inherited, True)
for name in option_names:
    env = dict(base, JAVA_TOOL_OPTIONS=retained)
    env[name] = (retained + " " if name == "JAVA_TOOL_OPTIONS" else "") + "-Djdk.serialFilter=" + strict_filter
    run("conflict-" + name.lower(), env, False)
run("changed-inherited-policy", dict(inherited, JAVA_TOOL_OPTIONS=guarded + " -Djdk.serialFilter=" + strict_filter), False)
