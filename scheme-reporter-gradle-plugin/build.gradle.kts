group = "org.tinywind"
version = "1.0.0"

repositories {
    mavenLocal()
    mavenCentral()
}

plugins {
    kotlin("jvm") version "1.9.22"
    `java-gradle-plugin`
    `maven-publish`
}

dependencies {
    implementation("org.tinywind:scheme-reporter:1.0.0")
    implementation("guru.nidi:graphviz-java-all-j2v8:0.18.1")
    implementation(gradleApi())
    implementation(localGroovy())
    testImplementation("junit:junit:4.13.1")
}

gradlePlugin {
    plugins {
        create("schemeReporterPlugin") {
            id = "org.tinywind.scheme-reporter"
            implementationClass = "org.tinywind.SchemeReporterPlugin"
        }
    }
}
