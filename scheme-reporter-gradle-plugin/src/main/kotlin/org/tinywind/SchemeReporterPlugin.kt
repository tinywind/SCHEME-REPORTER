package org.tinywind

import org.gradle.api.Plugin
import org.gradle.api.Project

open class SchemeReporterPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("schemaReporter", SchemeReporterExtension::class.java)
        val configuration = project.configurations.create("schemaReporter")
        configuration.description =
            "The classpath used to invoke the Schema Report generator. Add your JDBC driver, generator extensions, and additional dependencies here."

        project.tasks.register(
            "generateSchemeReport",
            SchemeReporterTask::class.java,
            extension,
            configuration,
        ).configure {
            it.group = "schema reporter"
            it.description = "Generates the Scheme Report"
        }
    }
}
