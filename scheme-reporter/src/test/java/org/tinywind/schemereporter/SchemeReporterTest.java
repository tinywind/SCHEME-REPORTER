/*
  Copyright (c) 2016, Jeon JaeHyeong (http://github.com/tinywind)
  All rights reserved.
  <p>
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  <p>
  http://www.apache.org/licenses/LICENSE-2.0
  <p>
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */
package org.tinywind.schemereporter;

import org.apache.tools.ant.filters.StringInputStream;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinywind.schemereporter.sample.Creator;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class SchemeReporterTest {
    private static final Logger log = LoggerFactory.getLogger(SchemeReporterTest.class);
    private final String header = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" +
            "<configuration>" +
            "    <jdbc>" +
            "        <driverClass>org.h2.Driver</driverClass>" +
            "    </jdbc>" +
            "    <database>" +
            "        <url>jdbc:h2:mem:test;DB_CLOSE_DELAY=-1</url>" +
            "        <user>sa</user>" +
            "        <password></password>" +
            "        <includes>.*</includes>" +
            "        <inputSchema>PUBLIC</inputSchema>" +
            "    </database>" +
            "    <generator>";
    private final String tail = "        <outputDirectory>doc</outputDirectory>" +
            "    </generator>" +
            "</configuration>";

    private final String pdfConfig = header + "<reporterClass>pdf</reporterClass>" + tail;
    private final String htmlConfig = header + "<reporterClass>html</reporterClass>" + tail;
    private final String excelConfig = header + "<reporterClass>excel</reporterClass>" + tail;
    private final String docxConfig = header + "<reporterClass>docx</reporterClass>" + tail;

    @Test
    public void test() throws SQLException, IOException {
        Creator.create();

        try {
            SchemeReporter.generate(SchemeReporter.load(new StringInputStream(htmlConfig)));
            assert new File("doc/PUBLIC.html").exists();
            SchemeReporter.generate(SchemeReporter.load(new StringInputStream(pdfConfig)));
            assert new File("doc/PUBLIC.pdf").exists();
            SchemeReporter.generate(SchemeReporter.load(new StringInputStream(excelConfig)));
            assert new File("doc/PUBLIC.xlsx").exists();
            SchemeReporter.generate(SchemeReporter.load(new StringInputStream(docxConfig)));
            assert new File("doc/PUBLIC.docx").exists();
        } catch (Exception e) {
            log.error("Error", e);
            assert false;
        } finally {
            new File("doc/PUBLIC.html").deleteOnExit();
            new File("doc/PUBLIC.pdf").deleteOnExit();
            new File("doc/PUBLIC.xlsx").deleteOnExit();
            new File("doc/PUBLIC.docx").deleteOnExit();
        }
    }
}
