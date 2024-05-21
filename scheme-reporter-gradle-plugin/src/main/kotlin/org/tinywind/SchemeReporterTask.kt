package org.tinywind

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.TaskAction
import org.tinywind.schemereporter.SchemeReporter
import org.tinywind.schemereporter.jaxb.Configuration
import java.net.URLClassLoader
import java.sql.Driver
import javax.inject.Inject

abstract class SchemeReporterTask @Inject constructor(
    private var extension: SchemeReporterExtension,
    private var runtimeClasspath: FileCollection,
) : DefaultTask() {

    @Suppress("UNCHECKED_CAST")
    private fun getDriverClass(driverClass: String): Class<out Driver> {
        val loader = URLClassLoader(runtimeClasspath.map { it.toURI().toURL() }.toTypedArray())
        loader.loadClass(driverClass).let {
            if (Driver::class.java.isAssignableFrom(it)) return it as Class<out Driver>
        }
        throw RuntimeException("Driver class not found")
    }

    @TaskAction
    fun run() {
        if (extension.generator.reporterClass.isNullOrBlank() || extension.generator.reporterClass == "org.tinywind.schemereporter.html.HtmlReporter") {
            logger.lifecycle("reporterClass: org.tinywind.schemereporter.html.HtmlReporter is not supported")
            extension.generator.reporterClass = "org.tinywind.schemereporter.pdf.PdfReporter"
        }

        val configuration = Configuration().apply {
            jdbc = extension.jdbc
            database = extension.database
            generator = extension.generator
        }
        SchemeReporter.generate(configuration) { config -> getDriverClass(config.jdbc.driverClass) }
    }
}
