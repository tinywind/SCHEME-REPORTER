# Dependency compatibility notes

The dependency update keeps the Java 17 target and the existing report, CLI, Maven, and Gradle interfaces. It does not publish new artifacts.

- iText 9 uses `simulateBold()` and `simulateItalic()` for the simulated font styles previously selected by `setBold()` and `setItalic()`.
- docx4j 17 uses the JAXB reference implementation. Loading both that provider and MOXy caused a `ClassCastException` when creating DOCX files.
- Graphviz uses the GraalJS engine without the obsolete J2V8 native runtime. The standalone assembly preserves the multi-release manifest and merges service descriptors as required by [GraalVM embedding guidance](https://github.com/oracle/graal/blob/master/docs/reference-manual/embedding/embed-languages.md#uber-jar-file-creation).
- The standalone distribution retains Derby 10.17.1.0, which requires Java 21. The other bundled JDBC drivers load on Java 17. The Derby integration test runs on Java 21 or later and is skipped on Java 17.
- The obsolete H2 1.4.196 test JAR is replaced by the declared H2 dependency. The optional CLI JDBC JAR argument remains supported.
- The Gradle wrapper is pinned to 9.8.2 with its distribution checksum. The plugin keeps a Java 17 toolchain.

## OGNL advisory disposition

Thymeleaf 3.1.5.RELEASE depends on OGNL 3.3.4. Package scanners report CVE-2026-113730 for that version. [The Thymeleaf maintainer states that Thymeleaf is unaffected](https://github.com/thymeleaf/thymeleaf/issues/1058#issuecomment-6484298481) because it does not use OGNL stricter invocation mode. This disposition applies only to the Thymeleaf use of OGNL and this advisory. It does not cover applications that call OGNL directly.

Forcing OGNL 3.4.13 with this released Thymeleaf version causes `NoSuchMethodError` during HTML generation. The compatible released dependency is retained until Thymeleaf releases its upstream OGNL 3.4 integration. No scanner ignore rule is added; raw findings remain visible.

## Kotlin build-tool advisory

The Kotlin Gradle plugin 2.4.21 embeds relocated Guava 33.3.1-jre classes. Binary scanning reports HIGH CVE-2026-102554. Inspection of the embedded `CompactHashMap` and `CompactHashSet` deserialization methods confirms that they lack the allocation limit from [the upstream Guava fix](https://github.com/google/guava/commit/b931fe9d6d5cf00bc55714ad3308d086f71850fe). A normal Guava dependency constraint does not replace these relocated classes.

[Kotlin 2.4.21 is the latest stable release checked on 2026-10-29](https://github.com/JetBrains/kotlin/releases/tag/v2.4.21). The vulnerable shaded bytes remain present, and the raw HIGH finding remains unsuppressed pending an upstream release with the dependency fixed. The Kotlin Gradle plugin builds the Gradle integration; it is not a dependency of the produced plugin POM or report runtime and is not part of the standalone distribution.

The wrapper scripts set `JAVA_TOOL_OPTIONS` before starting Java so a serialization filter rejects the three affected relocated collection families before Gradle initializes deserialization. Gradle applies a filter supplied only through `org.gradle.jvmargs` too late to activate it. The project `gradle.properties` disables persistent Gradle daemons and runs Kotlin compilation in the protected process. Tests pass the filter to their worker and consumer Gradle JVMs. `ShadedGuavaSerializationTest` reproduces the allocation failure for each family in an unprotected 32 MB child JVM, then confirms that the default build filter rejects the same payload. Each probe has a 20 second timeout. Run it with `./gradlew test --tests org.tinywind.ShadedGuavaSerializationTest` from the Gradle plugin directory.

Bytecode inspection found deserialization of compiler-produced metrics in `BtaCompilerRunnerKt.extractMetrics` and local KAPT cache files in `ClasspathSnapshot` and `ClasspathEntryData`. This project does not apply KAPT or Kotlin Native plugins. No network or report-input route into these Kotlin deserializers was demonstrated. Poisoned caches or a compromised compiler output remain relevant trust boundaries; this is a scoped mitigation, not a claim that the library is unreachable or patched.

The wrappers reject preconfigured serialization filters or filter factories in `JAVA_TOOL_OPTIONS`, `JDK_JAVA_OPTIONS`, `_JAVA_OPTIONS`, `JAVA_OPTS`, and `GRADLE_OPTS` instead of weakening an operator policy. Other Java options are preserved, including quoted values. An unchanged guard owned by a parent wrapper is accepted for nested calls. Operator policies that require additional restrictions need explicit composition with the bootstrap policy before building.

Developer builds must use the protected wrapper and retain the project settings. Bypassing the wrapper, dropping `JAVA_TOOL_OPTIONS`, reusing an older unprotected daemon, or using the Kotlin build plugin outside this project can remove that protection. Keep build caches private and do not restore caches from untrusted sources.

## Source analysis

The explicit CodeQL workflow compiles the Maven reactor and both Gradle plugin source sets before Java/Kotlin analysis. It also analyzes the repository Actions workflows, with PR query filtering disabled. Both validation workflows check out the exact PR head. Raw SARIF checks require nonzero Kotlin LOC and the exact plugin source paths. `SCHEME_CODEQL_UPLOAD` must remain unset until the replacement analysis has passed review; the existing default setup stays enabled during that verification.

## Verification scope

The compatibility checks cover actual HTML, PDF, XLSX, and DOCX exports from disposable H2 schemas, report contents and relationship images, custom HTML templates, XML configuration, JDBC driver loading, a Derby export on Java 21, packaged CLI usage and its JDBC JAR argument, the Maven generate goal and skip option, and the Gradle consumer task. External database server connections and publication are outside these checks.

The existing Maven goal logs report-generation failures and returns normally, so a Maven exit code of zero alone does not prove that a report was created. Negative checks must inspect the diagnostic and confirm that no report was produced.
