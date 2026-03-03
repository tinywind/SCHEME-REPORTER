package org.tinywind

import org.tinywind.schemereporter.jaxb.Database
import org.tinywind.schemereporter.jaxb.Generator
import org.tinywind.schemereporter.jaxb.Jdbc

open class SchemeReporterExtension {
    var jdbc: Jdbc = Jdbc()
    var database: Database = Database()
    var generator: Generator = Generator()

    fun jdbc(action: Jdbc.() -> Unit) {
        jdbc.apply(action)
    }

    fun database(action: Database.() -> Unit) {
        database.apply(action)
    }

    fun generator(action: Generator.() -> Unit) {
        generator.apply(action)
    }
}
