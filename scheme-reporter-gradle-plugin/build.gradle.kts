plugins {
    kotlin("jvm") version "2.1.0"
    `java-gradle-plugin`
    `maven-publish`
    id("com.gradle.plugin-publish") version "2.0.0"
}

group = "org.tinywind"
version = "1.0.2"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("org.tinywind:scheme-reporter:1.0.2")
    implementation("guru.nidi:graphviz-java-all-j2v8:0.18.1")
    implementation(gradleApi())
    testImplementation("junit:junit:4.13.2")
    testImplementation(gradleTestKit())
    testImplementation("org.tinywind:scheme-reporter-sample-database:1.0.2")
    testImplementation("com.h2database:h2:2.2.224")
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
