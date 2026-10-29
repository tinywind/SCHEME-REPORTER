# Run
mvn package org.apache.maven.plugins:maven-assembly-plugin:3.8.0:single

java -jar target/scheme-reporter-standalone-{version}-jar-with-dependencies.jar {configuration-xml} \[{jdbc-class-jar}\]

# Run Sample
java -jar target/scheme-reporter-standalone-2.0.0-jar-with-dependencies.jar "src/test/resources/sample-configuration.xml"

# Output(sample)
https://raw.githubusercontent.com/tinywind/SCHEME-REPORTER/master/sample-output.html

https://raw.githubusercontent.com/tinywind/SCHEME-REPORTER/master/sample-output.pdf

https://raw.githubusercontent.com/tinywind/SCHEME-REPORTER/master/sample-output.xlsx

https://raw.githubusercontent.com/tinywind/SCHEME-REPORTER/master/sample-output.docx


# LICENSE
**Licensed under the Apache License, Version 2.0**

If use on commercial databases, refer `http://www.jooq.org/legal/licensing`
