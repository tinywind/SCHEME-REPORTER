# SCHEME-REPORTER
create reporter from remote database

# Configure Maven
```
    <build>
        <plugins>
            <plugin>
                <groupId>org.tinywind</groupId>
                <artifactId>scheme-reporter-maven</artifactId>
                <version>0.3.1</version>
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
                        <url>jdbc:postgresql://localhost:5432/guidemon</url>
                        <user>postgres</user>
                        <password>1234</password>
                        <includes>.*</includes>
                        <excludes>schema_version|jettysessions|jettysessionids</excludes>
                        <inputSchema>information_schema</inputSchema>
                    </database>                
                    <generator>
                        <!--<reporterClass>org.tinywind.schemereporter.pdf.PdfReporter</reporterClass>-->
                        <!--<template>scheme-reporter/src/main/resources/asset/default.jsp</template>-->
                        <outputDirectory>doc</outputDirectory>
                    </generator>
                </configuration>
                <dependencies>
                    <dependency>
                        <groupId>org.postgresql</groupId>
                        <artifactId>postgresql</artifactId>
                        <version>9.4-1200-jdbc41</version>
                    </dependency>
                </dependencies>
            </plugin>
        </plugins>
    </build>
```

# Run
mvn scheme-reporter-maven:generate

# Output(sample)
https://raw.githubusercontent.com/tinywind/SCHEME-REPORTER/master/sample-output.html

https://raw.githubusercontent.com/tinywind/SCHEME-REPORTER/master/sample-output.pdf


# LICENSE
**Licensed under the Apache License, Version 2.0**

If use on commercial databases, refer `http://www.jooq.org/legal/licensing`
