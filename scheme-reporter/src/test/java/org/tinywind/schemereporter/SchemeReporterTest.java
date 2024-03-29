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
import org.tinywind.schemereporter.sample.Creator;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class SchemeReporterTest {
    private final String header = "" +
            "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" +
            "<configuration>" +
            "    <jdbc>" +
            "        <driverClass>org.h2.Driver</driverClass>" +
            "    </jdbc>" +
            "    <database>" +
            "        <url>jdbc:h2:tcp://localhost:9092/mem:test;DB_CLOSE_DELAY=-1</url>" +
            "        <user>sa</user>" +
            "        <password></password>" +
            "        <includes>.*</includes>" +
            "        <inputSchema>PUBLIC</inputSchema>" +
            "    </database>" +
            "    <generator>";
    private final String tail = "" +
            "        <outputDirectory>doc</outputDirectory>" +
            "    </generator>" +
            "</configuration>";

    private final String pdfConfig = header + "<reporterClass>org.tinywind.schemereporter.pdf.PdfReporter</reporterClass>" + tail;
    private final String htmlConfig = header + "<reporterClass>org.tinywind.schemereporter.html.HtmlReporter</reporterClass>" + tail;
    private final String excelConfig = header + "<reporterClass>org.tinywind.schemereporter.excel.ExcelReporter</reporterClass>" + tail;
    private final String docxConfig = header + "<reporterClass>org.tinywind.schemereporter.docx.DocxReporter</reporterClass>" + tail;

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
            e.printStackTrace();
            assert false;
        } finally {
            new File("doc/PUBLIC.html").deleteOnExit();
            new File("doc/PUBLIC.pdf").deleteOnExit();
            new File("doc/PUBLIC.xlsx").deleteOnExit();
            new File("doc/PUBLIC.docx").deleteOnExit();
        }
    }
}