# SCHEME-REPORTER

SCHEME-REPORTER is a tool designed to generate reports from remote databases in various formats such as HTML, PDF, Excel, and DOCX. It supports a flexible configuration and can be easily integrated into Maven or Gradle build systems.

## Features

- Supports multiple report formats: HTML, PDF, Excel, and DOCX
- Custom reporter class support
- Uses Thymeleaf as the HTML template engine
- Updated to support JDK 17
- Easily integrated with Maven and Gradle

## Configure Maven

To use SCHEME-REPORTER with Maven, add the following configuration to your `pom.xml` file:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.tinywind</groupId>
            <artifactId>scheme-reporter-maven</artifactId>
            <version>1.0.0</version>
            <executions>
                <execution>
                    <phase>none</phase>
                </execution>
            </executions>
            <configuration>
                <jdbc>
                    <driverClass>org.h2.Driver</driverClass>
                </jdbc>
                <database>
                    <url>jdbc:h2:mem:test;DB_CLOSE_DELAY=-1</url>
                    <user>sa</user>
                    <password></password>
                    <includes>.*</includes>
                    <excludes>schema_version|jettysessions|jettysessionids</excludes>
                    <inputSchema>PUBLIC</inputSchema>
                </database>
                <generator>
                    <!-- Available formats: html, pdf, excel, docx -->
                    <!-- Or use a custom reporter class implementing the Reportable interface -->
                    <reporterClass>pdf</reporterClass>
                    <!-- Thymeleaf template file for HTML reporter class. If not set, the default template will be used. -->
                    <!-- Refer to: https://raw.githubusercontent.com/tinywind/SCHEME-REPORTER/master/scheme-reporter/src/main/resources/asset/default.html -->
                    <template>./asset/template.html</template>
                    <outputDirectory>doc</outputDirectory>
                </generator>
            </configuration>
            <dependencies>
                <dependency>
                    <groupId>com.h2database</groupId>
                    <artifactId>h2</artifactId>
                    <version>2.2.224</version>
                </dependency>
            </dependencies>
        </plugin>
    </plugins>
</build>
```

### Running the Maven Plugin

To generate a report using Maven, run the following command:

```sh
mvn scheme-reporter-maven:generate
```

## Configure Gradle

To use SCHEME-REPORTER with Gradle, add the following configuration to your `build.gradle.kts` file:

```kotlin
plugins {
    id("org.tinywind.scheme-reporter") version "1.0.0"
}

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

### Running the Gradle Task

To generate a report using Gradle, run the following command:

```sh
./gradlew generateSchemeReport
```

For more details on using the Scheme Reporter Gradle Plugin, refer to the [Scheme Reporter Gradle Plugin README](https://github.com/tinywind/SCHEME-REPORTER/blob/master/scheme-reporter-gradle-plugin/README.md).

## Output Samples

Here are some sample outputs generated by SCHEME-REPORTER:

- [HTML Report](https://raw.githubusercontent.com/tinywind/SCHEME-REPORTER/master/sample-output.html)
- [PDF Report](https://raw.githubusercontent.com/tinywind/SCHEME-REPORTER/master/sample-output.pdf)
- [Excel Report](https://raw.githubusercontent.com/tinywind/SCHEME-REPORTER/master/sample-output.xlsx)
- [DOCX Report](https://raw.githubusercontent.com/tinywind/SCHEME-REPORTER/master/sample-output.docx)

## License

Licensed under the Apache License, Version 2.0. If you are using this tool with commercial databases, please refer to the [jOOQ licensing page](http://www.jooq.org/legal/licensing) for more information.

## Contribution

Contributions are welcome! If you find any issues or have suggestions, please open an issue or submit a pull request on GitHub.

---
