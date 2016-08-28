/**
 * Copyright (c) 2016, Jeon JaeHyeong (http://github.com/tinywind)
 * All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tinywind.schemereporter;

import org.apache.tools.ant.filters.StringInputStream;
import org.junit.Test;

import java.io.File;

public class PdfTest {
    private final String header = "" +
            "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" +
            "<configuration>" +
            "    <jdbc>" +
            "        <driverClass>org.postgresql.Driver</driverClass>" +
            "    </jdbc>" +
            "    <database>" +
            "        <url>jdbc:postgresql://localhost:5432/guidemon</url>" +
            "        <user>postgres</user>" +
            "        <password>1234</password>" +
            "        <includes>.*</includes>" +
            "        <excludes>schema_version|jettysessions|jettysessionids</excludes>" +
            "        <inputSchema>public</inputSchema>" +
            "    </database>" +
            "    <generator>";
    private final String tail = "" +
            "        <outputDirectory>doc</outputDirectory>" +
            "    </generator>" +
            "</configuration>";

    private final String pdfConfig = header + "<reporterClass>org.tinywind.schemereporter.pdf.PdfReporter</reporterClass>" + tail;
    private final String htmlConfig = header + "<reporterClass>org.tinywind.schemereporter.html.HtmlReporter</reporterClass>" + tail;

    @Test
    public void test() {
        try {
            SchemeReporter.generate(SchemeReporter.load(new StringInputStream(htmlConfig)));
            assert new File("doc/public.html").exists();
            SchemeReporter.generate(SchemeReporter.load(new StringInputStream(pdfConfig)));
            assert new File("doc/public.pdf").exists();
        } catch (Exception e) {
            e.printStackTrace();
            assert false;
        } finally {
            new File("doc/public.html").delete();
            new File("doc/public.pdf").delete();
        }
    }
}