import java.io.ObjectInputFilter

plugins {
    kotlin("jvm") version "2.4.21"
    `java-gradle-plugin`
    `maven-publish`
    id("com.gradle.plugin-publish") version "2.2.1"
}

group = "org.tinywind"
version = "2.0.0"

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("org.tinywind:scheme-reporter:2.0.0")
    implementation(gradleApi())
    testImplementation("junit:junit:4.13.2")
    testImplementation(gradleTestKit())
    testImplementation("org.tinywind:scheme-reporter-sample-database:2.0.0")
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
