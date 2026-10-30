# SCHEME-REPORTER

Generate database schema reports in HTML, PDF, Excel, and DOCX with Maven or Gradle.

## Features

- Supports multiple report formats: HTML, PDF, Excel, and DOCX
- Custom reporter class support
- Uses Thymeleaf as the HTML template engine
- Maven and Gradle integration

## Compatibility

| Component | Supported versions |
|---|---|
| JDK | 21+ |
| Gradle | 8.6 (JDK 21), 9.8.2 (JDK 21/25) |
| Maven | 3.10+ |

## Gradle

Add the plugin and your JDBC driver to `build.gradle.kts`:

```kotlin
plugins {
    id("org.tinywind.scheme-reporter") version "2.0.0"
}

repositories {
    mavenCentral()
}

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
        // Available formats: pdf, docx, excel, html
        // Or use a fully qualified class name implementing the Reportable interface
        reporterClass = "pdf"
        outputDirectory = layout.buildDirectory.dir("reports/schema").get().asFile.absolutePath
    }
}
```

### Run the Gradle task

```sh
./gradlew generateSchemeReport
```

For more details on using the Scheme Reporter Gradle Plugin, refer to the [Scheme Reporter Gradle Plugin README](https://github.com/tinywind/SCHEME-REPORTER/blob/main/scheme-reporter-gradle-plugin/README.md).

## Maven

Add the following configuration to your `pom.xml` file:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.tinywind</groupId>
            <artifactId>scheme-reporter-maven</artifactId>
            <version>2.0.0</version>
            <executions>
                <execution>
                    <phase>none</phase>
                </execution>
            </executions>
            <configuration>
                <jdbc>
                    <driverClass>org.postgresql.Driver</driverClass>
                </jdbc>
                <database>
                    <url>jdbc:postgresql://localhost:5432/mydb</url>
                    <user>postgres</user>
                    <password>postgres</password>
                    <includes>.*</includes>
                    <excludes>flyway_schema_history</excludes>
                    <inputSchema>public</inputSchema>
                </database>
                <generator>
                    <!-- Available formats: html, pdf, excel, docx -->
                    <!-- Or use a fully qualified class name implementing the Reportable interface -->
                    <reporterClass>pdf</reporterClass>
                    <!-- Thymeleaf template file for HTML reporter class. If not set, the default template will be used. -->
                    <template>./asset/template.html</template>
                    <outputDirectory>doc</outputDirectory>
                </generator>
            </configuration>
            <dependencies>
                <dependency>
                    <groupId>org.postgresql</groupId>
                    <artifactId>postgresql</artifactId>
                    <version>42.7.10</version>
                </dependency>
            </dependencies>
        </plugin>
    </plugins>
</build>
```

### Run the Maven goal

```sh
mvn org.tinywind:scheme-reporter-maven:2.0.0:generate
```

## Output samples

- [HTML Report](https://raw.githubusercontent.com/tinywind/SCHEME-REPORTER/main/sample-output.html)
- [PDF Report](https://raw.githubusercontent.com/tinywind/SCHEME-REPORTER/main/sample-output.pdf)
- [Excel Report](https://raw.githubusercontent.com/tinywind/SCHEME-REPORTER/main/sample-output.xlsx)
- [DOCX Report](https://raw.githubusercontent.com/tinywind/SCHEME-REPORTER/main/sample-output.docx)

## License

Licensed under the Apache License, Version 2.0. If you are using this tool with commercial databases, please refer to the [jOOQ licensing page](http://www.jooq.org/legal/licensing) for more information.

## Contributing

Open an issue to report a problem or propose a change, or submit a pull request.
