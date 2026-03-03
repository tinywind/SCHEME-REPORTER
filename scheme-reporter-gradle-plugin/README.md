# Scheme Reporter Gradle Plugin

The Scheme Reporter Gradle Plugin generates database schema reports in various formats such as PDF, DOCX, Excel, and HTML.

## Prerequisites

- **JDK**: 17 or later
- **Gradle**: 8.x or 9.x

## Installation

Add the plugin to your `build.gradle.kts`:

```kotlin
plugins {
    id("org.tinywind.scheme-reporter") version "1.0.1"
}

repositories {
    mavenCentral()
}
```

## Configuration

Add your JDBC driver and configure the plugin:

```kotlin
dependencies {
    schemaReporter("org.postgresql:postgresql:42.7.10")
}

schemaReporter {
    jdbc {
        driverClass = "org.postgresql.Driver"
    }
    database {
        url = "jdbc:postgresql://localhost:5432/mydb"
        user = "postgres"
        password = "postgres"
        includes = ".*"
        excludes = "flyway_schema_history"
        inputSchema = "public"
    }
    generator {
        reporterClass = "pdf"
        outputDirectory = layout.buildDirectory.dir("reports/schema").get().asFile.absolutePath
    }
}
```

### Configuration Reference

- **jdbc**
    - `driverClass` - Fully qualified name of the JDBC driver class
- **database**
    - `url` - JDBC URL of the database
    - `user` - Database username
    - `password` - Database password
    - `includes` - Regex pattern for tables to include (e.g. `.*`)
    - `excludes` - Regex pattern for tables to exclude (e.g. `flyway_schema_history`)
    - `inputSchema` - Target schema name
- **generator**
    - `reporterClass` - Report format: `pdf`, `docx`, `excel`, `html`, or a fully qualified class name implementing the `Reportable` interface
    - `template` - (Optional) Thymeleaf template file path for the HTML reporter. Uses the default template if not set
    - `outputDirectory` - Directory where the generated reports will be saved

### MySQL Example

```kotlin
dependencies {
    schemaReporter("com.mysql:mysql-connector-j:8.4.0")
}

schemaReporter {
    jdbc {
        driverClass = "com.mysql.cj.jdbc.Driver"
    }
    database {
        url = "jdbc:mysql://localhost:3306/mydb"
        user = "root"
        password = "password"
        inputSchema = "mydb"
    }
    generator {
        reporterClass = "pdf"
        outputDirectory = layout.buildDirectory.dir("reports/schema").get().asFile.absolutePath
    }
}
```

## Usage

```sh
./gradlew generateSchemeReport
```

## License

**Licensed under the Apache License, Version 2.0**

If use on commercial databases, refer `http://www.jooq.org/legal/licensing`

---
