# Scheme Reporter Gradle Plugin

The Scheme Reporter Gradle Plugin allows you to generate database schema reports in various formats such as PDF, DOCX, Excel, and HTML. You can also use custom reporter classes to implement your own report formats.

## Getting Started

### Prerequisites

- Gradle 8.0 or later
- Java 17 or later

### Installation

1. Add the plugin to your `build.gradle.kts` file.

```kotlin
plugins {
    id("org.tinywind.scheme-reporter") version "1.0.0"
}

repositories {
    mavenCentral()
}
```

2. Add the necessary dependencies and configure the `schemaReporter` extension in your `build.gradle.kts`.

```kotlin
dependencies {
    schemaReporter("com.mysql:mysql-connector-j:8.4.0")
}

schemaReporter {
    jdbc {
        driverClass = "com.mysql.cj.jdbc.Driver"
    }
    database {
        url = "jdbc:mysql://127.0.0.1:3306/database"
        user = "user"
        password = "password"
        inputSchema = "database"
    }
    generator {
        // Available formats: pdf, docx, excel, html
        // Or use a custom reporter class implementing the Reportable interface
        reporterClass = "pdf"
        // Thymeleaf template file for HTML reporter class. If not set, the default template will be used.
        // Refer to: https://raw.githubusercontent.com/tinywind/SCHEME-REPORTER/master/scheme-reporter/src/main/resources/asset/default.html
        template = "./asset/template.html"
        outputDirectory = "/doc"
    }
}
```

### Configuration

- `schemaReporter`: Main configuration block for the plugin.
    - `jdbc`: Configuration for the JDBC connection.
        - `driverClass`: Fully qualified name of the JDBC driver class.
    - `database`: Configuration for the database connection.
        - `url`: JDBC URL of the database.
        - `user`: Database username.
        - `password`: Database password.
        - `inputSchema`: Input schema for the database.
    - `generator`: Configuration for the report generator.
        - `reporterClass`: Class name or format (pdf, docx, excel, html) of the reporter.
        - `template`: Path to the Thymeleaf template file for the HTML reporter.
        - `outputDirectory`: Directory where the generated reports will be saved.

### Usage

To generate the schema report, run the following Gradle task:

```sh
./gradlew generateSchemeReport
```

This will generate the report in the specified `outputDirectory`.

### Additional Configuration

If the plugin does not process correctly, you may need to add `mavenCentral()` to the `pluginManagement` section of the `repositories` in your Gradle settings.

```kotlin
pluginManagement {
    repositories {
        mavenCentral() // note: add this repository
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### Contributing

Contributions are welcome! Please open an issue or submit a pull request on GitHub.

# LICENSE
**Licensed under the Apache License, Version 2.0**

If use on commercial databases, refer `http://www.jooq.org/legal/licensing`

---
