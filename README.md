#SCHEME-REPORTER
create reporter from remote database

#Configure Maven
```
    <build>
        <plugins>
            <plugin>
                <groupId>org.tinywind</groupId>
                <artifactId>scheme-reporter-maven</artifactId>
                <version>0.1</version>
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
                        <outputDirectory>doc</outputDirectory>
                        <!--<jspTemplate>scheme-reporter/src/main/resources/asset/default.jsp</jspTemplate>-->
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

#Run
mvn org.tinywind:scheme-reporter-maven:0.1:generate

#Output(sample)
https://raw.githubusercontent.com/tinywind/SCHEME-REPORTER/master/sample-output.html

#LICENSE
**Licensed under the Apache License, Version 2.0**