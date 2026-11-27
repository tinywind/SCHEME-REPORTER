package org.tinywind

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import org.tinywind.schemereporter.SchemeReporter
import org.tinywind.schemereporter.jaxb.Configuration
import java.io.File
import java.net.URLClassLoader
import java.sql.Driver
import javax.inject.Inject

@DisableCachingByDefault(because = "Reads schema metadata from a live JDBC connection")
abstract class SchemeReporterTask @Inject constructor(
    @get:Internal
    private val extension: SchemeReporterExtension,
    @get:Classpath
    private val runtimeClasspath: FileCollection,
) : DefaultTask() {

    @TaskAction
    fun run() {
        if (extension.generator.reporterClass.isNullOrBlank()) {
            extension.generator.reporterClass = "pdf"
        }

        val configuration = Configuration().apply {
            jdbc = extension.jdbc
            database = extension.database
            generator = extension.generator
        }
        withDriverClassLoader(runtimeClasspath) { loader ->
            SchemeReporter.generate(configuration) { config ->
                loader.loadClass(config.jdbc.driverClass).asSubclass(Driver::class.java)
            }
        }
    }
}

internal fun withDriverClassLoader(classpath: Iterable<File>, generate: (URLClassLoader) -> Unit) {
    val urls = classpath.map { it.toURI().toURL() }.toTypedArray()
    URLClassLoader(urls).use(generate)
}
