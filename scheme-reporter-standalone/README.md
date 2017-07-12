# Run
mvn org.apache.maven.plugins:maven-assembly-plugin:2.2-beta-5:assembly

java -jar target/scheme-reporter-standalone-{version}-jar-with-dependencies.jar {configuration-xml} \[{jdbc-class-jar}\]

# Run Sample
java -jar target/scheme-reporter-standalone-0.4.0-jar-with-dependencies.jar "src/test/resources/sample-configuration.xml" "src/test/resources/h2-1.4.196.jar"

# Output(sample)
https://raw.githubusercontent.com/tinywind/SCHEME-REPORTER/master/sample-output.html

https://raw.githubusercontent.com/tinywind/SCHEME-REPORTER/master/sample-output.pdf


# LICENSE
**Licensed under the Apache License, Version 2.0**

If use on commercial databases, refer `http://www.jooq.org/legal/licensing`
