import java.io.ObjectInputFilter

plugins {
    kotlin("jvm") version "2.4.21"
    `java-gradle-plugin`
    `maven-publish`
    id("com.gradle.plugin-publish") version "2.2.1"
}

group = "org.tinywind"
version = "2.0.2"

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("org.tinywind:scheme-reporter:2.0.2")
    implementation(gradleApi())
    testImplementation("junit:junit:4.13.2")
    testImplementation(gradleTestKit())
    testImplementation("org.tinywind:scheme-reporter-sample-database:2.0.2")
    testImplementation("com.h2database:h2:2.5.252")
}

tasks.test {
    check(ObjectInputFilter.Config.getSerialFilter() != null) { "Use the protected Gradle wrapper for this build" }
    System.getProperty("jdk.serialFilter")?.let { systemProperty("jdk.serialFilter", it) }
    val kotlinPlugin = buildscript.configurations.getByName("classpath").resolvedConfiguration.resolvedArtifacts.single {
        it.moduleVersion.id.group == "org.jetbrains.kotlin" && it.name == "kotlin-gradle-plugin"
    }
    systemProperty("schemeReporterKotlinPluginJar", kotlinPlugin.file.absolutePath)
}

tasks.wrapper {
    doLast {
        val buildFilter = "!org.jetbrains.kotlin.com.google.common.collect.CompactHashMap;!org.jetbrains.kotlin.com.google.common.collect.CompactHashSet;!org.jetbrains.kotlin.com.google.common.collect.MapMakerInternalMap\$*;*"
        val unixScript = scriptFile.readText(Charsets.UTF_8)
        if (!unixScript.contains("SCHEME_REPORTER_SERIAL_FILTER")) {
            val unixAnchor = "# Attempt to set APP_HOME\n"
            check(unixScript.contains(unixAnchor)) { "Unexpected gradlew template: missing APP_HOME anchor" }
            val unixGuard = listOf(
                "# Block the vulnerable Guava serializers shaded by the Kotlin build plugin.",
                "scheme_build_filter='$buildFilter'",
                "scheme_filter_conflict() {",
                "    printf '%s\\n' \"A preconfigured serialization filter requires explicit policy composition; refusing to replace it.\" >&2",
                "    exit 1",
                "}",
                "for scheme_jvm_options in \"\${JAVA_OPTS-}\" \"\${GRADLE_OPTS-}\" \"\${JDK_JAVA_OPTIONS-}\" \"\${_JAVA_OPTIONS-}\"; do",
                "    case \$scheme_jvm_options in",
                "      *-Djdk.serialFilter*) scheme_filter_conflict ;;",
                "    esac",
                "done",
                "if [ \"\${SCHEME_REPORTER_SERIAL_FILTER-}\" = \"\$scheme_build_filter\" ] &&",
                "   [ \"\${SCHEME_REPORTER_GUARDED_JAVA_TOOL_OPTIONS-}\" = \"\${JAVA_TOOL_OPTIONS-}\" ]; then",
                "    :",
                "else",
                "    case \${JAVA_TOOL_OPTIONS-} in",
                "      *-Djdk.serialFilter*) scheme_filter_conflict ;;",
                "    esac",
                "    JAVA_TOOL_OPTIONS=\"\${JAVA_TOOL_OPTIONS:+\$JAVA_TOOL_OPTIONS }-Djdk.serialFilter=\$scheme_build_filter\"",
                "    SCHEME_REPORTER_SERIAL_FILTER=\$scheme_build_filter",
                "    SCHEME_REPORTER_GUARDED_JAVA_TOOL_OPTIONS=\$JAVA_TOOL_OPTIONS",
                "    export JAVA_TOOL_OPTIONS SCHEME_REPORTER_SERIAL_FILTER SCHEME_REPORTER_GUARDED_JAVA_TOOL_OPTIONS",
                "fi",
                "",
            ).joinToString("\n")
            scriptFile.writeText(unixScript.replaceFirst(unixAnchor, unixGuard + "\n" + unixAnchor), Charsets.UTF_8)
            scriptFile.setExecutable(true)
        }

        val winScript = batchScript.readText(Charsets.UTF_8)
        if (!winScript.contains("SCHEME_REPORTER_SERIAL_FILTER")) {
            val winSetlocalTarget = "@rem Set local scope for the variables, and ensure extensions are enabled\r\nsetlocal EnableExtensions\r\n"
            val winExecTarget = listOf(
                "@rem Execute gradlew",
                "@rem endlocal doesn't take effect until after the line is parsed and variables are expanded",
                "@rem which allows us to clear the local environment before executing the java command",
                "endlocal & \"%JAVA_EXE%\" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% \"-Dorg.gradle.appname=%APP_BASE_NAME%\" -jar \"%APP_HOME%\\gradle\\wrapper\\gradle-wrapper.jar\" %* & call :exitWithErrorLevel & goto exitWithErrorLevel",
            ).joinToString("\r\n")
            check(winScript.contains(winSetlocalTarget)) { "Unexpected gradlew.bat template: missing setlocal anchor" }
            check(winScript.contains(winExecTarget)) { "Unexpected gradlew.bat template: missing execute anchor" }
            val winGuard = listOf(
                "@rem Set local scope for the variables, and ensure extensions are enabled",
                "setlocal EnableExtensions DisableDelayedExpansion",
                "@rem Block the vulnerable Guava serializers shaded by the Kotlin build plugin.",
                "set \"SCHEME_REPORTER_BUILD_FILTER=$buildFilter\"",
                "setlocal EnableDelayedExpansion",
                "for %%V in (JAVA_OPTS GRADLE_OPTS JDK_JAVA_OPTIONS _JAVA_OPTIONS) do (",
                "    set \"SCHEME_REPORTER_JVM_OPTIONS=!%%V!\"",
                "    if defined SCHEME_REPORTER_JVM_OPTIONS (",
                "        if not \"!SCHEME_REPORTER_JVM_OPTIONS:-Djdk.serialFilter=!\"==\"!SCHEME_REPORTER_JVM_OPTIONS!\" (",
                "            set \"SCHEME_REPORTER_FILTER_SOURCE=%%V\"",
                "            goto serialFilterConflict",
                "        )",
                "    )",
                ")",
                "if \"!SCHEME_REPORTER_SERIAL_FILTER!\"==\"!SCHEME_REPORTER_BUILD_FILTER!\" if \"!JAVA_TOOL_OPTIONS!\"==\"!SCHEME_REPORTER_GUARDED_JAVA_TOOL_OPTIONS!\" goto serialFilterReady",
                "set \"SCHEME_REPORTER_FILTER_SOURCE=JAVA_TOOL_OPTIONS\"",
                "if defined JAVA_TOOL_OPTIONS if not \"!JAVA_TOOL_OPTIONS:-Djdk.serialFilter=!\"==\"!JAVA_TOOL_OPTIONS!\" goto serialFilterConflict",
                "set \"JAVA_TOOL_OPTIONS=!JAVA_TOOL_OPTIONS! -Djdk.serialFilter=!SCHEME_REPORTER_BUILD_FILTER!\"",
                "set \"SCHEME_REPORTER_SERIAL_FILTER=!SCHEME_REPORTER_BUILD_FILTER!\"",
                "set \"SCHEME_REPORTER_GUARDED_JAVA_TOOL_OPTIONS=!JAVA_TOOL_OPTIONS!\"",
                "goto serialFilterReady",
                ":serialFilterConflict",
                "1>&2 echo A preconfigured serialization filter in %SCHEME_REPORTER_FILTER_SOURCE% requires explicit policy composition; refusing to replace it.",
                "\"%COMSPEC%\" /c exit 1",
                "goto exitWithErrorLevel",
                ":serialFilterReady",
                "setlocal DisableDelayedExpansion",
                "",
            ).joinToString("\r\n")
            val winExecReplacement = listOf(
                "@rem Execute gradlew",
                "@rem Keep the serialization filter in the environment until the JVM exits.",
                "\"%JAVA_EXE%\" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% \"-Dorg.gradle.appname=%APP_BASE_NAME%\" -jar \"%APP_HOME%\\gradle\\wrapper\\gradle-wrapper.jar\" %*",
                "set \"SCHEME_REPORTER_EXIT_CODE=%ERRORLEVEL%\"",
                "endlocal & \"%COMSPEC%\" /c exit %SCHEME_REPORTER_EXIT_CODE% & call :exitWithErrorLevel & goto exitWithErrorLevel",
            ).joinToString("\r\n")
            batchScript.writeText(
                winScript
                    .replaceFirst(winSetlocalTarget, winGuard)
                    .replaceFirst(winExecTarget, winExecReplacement),
                Charsets.UTF_8,
            )
        }
    }
}

gradlePlugin {
    website.set("https://github.com/tinywind/SCHEME-REPORTER")
    vcsUrl.set("https://github.com/tinywind/SCHEME-REPORTER")
    plugins {
        create("schemeReporterPlugin") {
            id = "org.tinywind.scheme-reporter"
            implementationClass = "org.tinywind.SchemeReporterPlugin"
            displayName = "Scheme Reporter plugin"
            description = "Gradle plugin to generate scheme report"
            tags.set(listOf("database", "scheme", "report"))
        }
    }
}
