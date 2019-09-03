# SCHEME-REPORTER
create reporter from remote database

# Configure Maven
```
    <build>
        <plugins>
            <plugin>
                <groupId>org.tinywind</groupId>
                <artifactId>scheme-reporter-maven</artifactId>
                <version>0.5.0</version>
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
                        <url>jdbc:h2:tcp://localhost:9092/mem:test;DB_CLOSE_DELAY=-1</url>
                        <user>sa</user>
                        <password></password>
                        <includes>.*</includes>
                        <excludes>schema_version|jettysessions|jettysessionids</excludes>
                        <inputSchema>PUBLIC</inputSchema>
                    </database>                
                    <generator>
                        <!--<reporterClass>org.tinywind.schemereporter.pdf.PdfReporter</reporterClass>-->
                        <!--<reporterClass>org.tinywind.schemereporter.excel.ExcelReporter</reporterClass>-->
                        <!--<reporterClass>org.tinywind.schemereporter.docx.DocxReporter</reporterClass>-->
                        <!--<template>scheme-reporter/src/main/resources/asset/default.jsp</template>-->
                        <outputDirectory>doc</outputDirectory>
                    </generator>
                </configuration>
                <dependencies>
                    <dependency>
                        <groupId>com.h2database</groupId>
                        <artifactId>h2</artifactId>
                        <version>1.4.196</version>
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

https://raw.githubusercontent.com/tinywind/SCHEME-REPORTER/master/sample-output.xlsx

https://raw.githubusercontent.com/tinywind/SCHEME-REPORTER/master/sample-output.docx


# LICENSE
**Licensed under the Apache License, Version 2.0**

If use on commercial databases, refer `http://www.jooq.org/legal/licensing`
