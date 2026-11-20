# Security Policy

## Supported Versions

Security fixes are applied on `main` and released on the active `2.0.x` Java 21
release line across all modules published from this repository
(`scheme-reporter`, `scheme-reporter-maven`, `scheme-reporter-standalone`, and
`scheme-reporter-gradle-plugin`).

| Version | Supported |
| --- | --- |
| `2.0.x` (latest release / `main`) | Yes |
| `< 2.0.0` (legacy `0.x` releases) | No |

Supported runtime and build environments are JDK 21+, Maven 3.10+, and Gradle
8.6 (JDK 21) or 9.9.1 (JDK 21/25).

## Reporting a Vulnerability

Please report suspected security vulnerabilities privately through GitHub
Private Vulnerability Reporting:

- https://github.com/tinywind/SCHEME-REPORTER/security/advisories/new

Do **not** open a public GitHub issue, pull request, or discussion for an
undisclosed vulnerability.

When submitting a report, include as much of the following as you can:

- Version and distribution (`scheme-reporter`, `scheme-reporter-maven`,
  `scheme-reporter-standalone`, or `scheme-reporter-gradle-plugin`, plus the
  release tag or commit SHA).
- Environment details (JDK version, OS, Maven or Gradle version, and JDBC driver
  or database dialect if relevant).
- Affected component (for example: XML configuration parsing and JAXB
  unmarshalling, JDBC driver or reporter class loading, HTML/Thymeleaf template
  processing, Graphviz/Batik SVG-to-PNG rendering, PDF/DOCX/XLSX report
  generation, output file path handling, or the Gradle wrapper serialization
  filter guard).
- Minimal reproduction steps or proof of concept.
- Expected security impact.

**Do not attach real secrets** such as database passwords, production JDBC
connection strings, GPG signing keys, or confidential schema dumps.

## Scope

### In scope

Issues in `tinywind/SCHEME-REPORTER`, including:

- Unsafe XML or configuration handling in `SchemeReporter.load` or the
  standalone, Maven, and Gradle entry points.
- Output escaping, injection, or rendering vulnerabilities in generated HTML,
  SVG, PDF, DOCX, or XLSX reports caused by schema metadata.
- Unexpected file writes or path traversal derived from schema names, versions,
  or output directory handling.
- Unintended exposure of database credentials or sensitive configuration in logs
  or generated artifacts.
- Regressions or bypasses of the Gradle wrapper serialization-filter guard
  (`jdk.serialFilter` in `scheme-reporter-gradle-plugin/gradlew` and
  `gradlew.bat`).
- Dependency vulnerabilities that affect published `2.0.x` artifacts or the
  standalone distribution.

### Out of scope

- Vulnerabilities in third-party JDBC drivers, database servers, or upstream
  libraries (such as jOOQ, Thymeleaf, iText, docx4j, Apache POI, Batik,
  Graphviz, Maven, or Gradle) that do not stem from `SCHEME-REPORTER`'s usage.
- Code execution via explicitly operator-configured custom `reporterClass`
  classes, custom JDBC driver JARs, or untrusted Thymeleaf templates and build
  scripts (these are trusted operator inputs by design).
- Builds of `scheme-reporter-gradle-plugin` that bypass the protected Gradle
  wrapper, drop `JAVA_TOOL_OPTIONS`, reuse an unprotected daemon, or restore
  untrusted build caches.
- Unsupported legacy versions (`< 2.0.0`).
- Automated scanner findings without a demonstrated impact in `SCHEME-REPORTER`
  (see [DEPENDENCY_UPGRADE.md](DEPENDENCY_UPGRADE.md) for documented upstream
  advisory dispositions, including OGNL under Thymeleaf and shaded Guava in the
  Kotlin Gradle build plugin).

## Response and Disclosure

`SCHEME-REPORTER` is maintained on a best-effort basis:

- **Acknowledgement**: we aim to respond within 7 days in the private advisory
  thread.
- **Remediation**: confirmed vulnerabilities are prioritized by severity and
  fixed on `main`, followed by a `2.0.x` release.
- **Disclosure**: please allow reasonable time for a fix and release before
  public disclosure. Once resolved, a GitHub Security Advisory may be published,
  with credit if you would like to be acknowledged.

## Security Maintenance

Dependency compatibility notes, documented upstream advisory dispositions,
Gradle wrapper serialization-filter details, and the explicit CodeQL source
analysis workflow (`.github/workflows/codeql-source.yml`) are maintained in
[DEPENDENCY_UPGRADE.md](DEPENDENCY_UPGRADE.md).
