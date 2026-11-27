package org.tinywind.schemereporter;

import org.jooq.meta.SchemaDefinition;
import org.junit.Test;
import org.tinywind.schemereporter.jaxb.Configuration;
import org.tinywind.schemereporter.jaxb.Database;
import org.tinywind.schemereporter.jaxb.Generator;
import org.tinywind.schemereporter.jaxb.Jdbc;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.UUID;

import static org.junit.Assert.*;

public class ConfigurationLifecycleTest {
    private static final String SETTINGS = "<jdbc><driverClass>org.h2.Driver</driverClass></jdbc>"
            + "<database><url>jdbc:h2:mem:test</url><user>sa</user><password/></database>";

    @Test
    public void rejectsMissingSettingsBeforeLoadingADriver() {
        assertThrows(IllegalArgumentException.class, () -> SchemeReporter.generate(new Configuration(),
                unused -> { throw new AssertionError("driver loaded before validation"); }));
    }

    @Test
    public void coreCommandReturnsFailureForAMissingFile() throws Exception {
        Process process = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-cp", System.getProperty("java.class.path"), SchemeReporter.class.getName(),
                "absent-" + UUID.randomUUID() + ".xml")
                .redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals(output, 1, process.waitFor());
        assertTrue(output, output.contains("Cannot find"));
    }

    @Test
    public void acceptsLegacyNamespacesAndOptionalGenerator() throws Exception {
        for (String namespace : new String[]{"", " xmlns='urn:legacy'"}) {
            Configuration config = load("<configuration" + namespace + ">" + SETTINGS + "</configuration>");
            assertEquals("org.h2.Driver", config.getJdbc().getDriverClass());
        }
    }

    @Test
    public void leavesTheCallersConfigurationStreamOpen() throws IOException {
        var stream = new ByteArrayInputStream(("<configuration>" + SETTINGS + "</configuration>")
                .getBytes(StandardCharsets.UTF_8)) {
            @Override
            public void close() { fail("The caller owns this stream"); }
        };
        SchemeReporter.load(stream);
    }

    @Test
    public void rejectsDoctypesAndInvalidSettings() {
        assertThrows(IOException.class, () -> load("<!DOCTYPE configuration><configuration>"
                + SETTINGS + "</configuration>"));
        assertThrows(IOException.class, () -> load("<configuration>" + SETTINGS
                + "<unexpected/></configuration>"));
    }

    private static Configuration load(String xml) throws IOException {
        return SchemeReporter.load(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void releasesReporterAndConnectionOnSuccessAndOnFailure() throws Exception {
        String url = "jdbc:h2:mem:" + UUID.randomUUID();
        for (boolean fail : new boolean[]{false, true}) {
            try (Connection observer = DriverManager.getConnection(url, "sa", "")) {
                RecordingReporter.closed = false;
                Configuration config = new Configuration().withJdbc(new Jdbc().withDriverClass("org.h2.Driver"))
                        .withDatabase(new Database().withUrl(url).withUser("sa").withPassword("").withInputSchema("PUBLIC"))
                        .withGenerator(new Generator().withReporterClass(RecordingReporter.class.getName())
                                .withTemplate(fail ? "fail" : "pass"));
                if (fail) {
                    RuntimeException failure = assertThrows(RuntimeException.class, () -> SchemeReporter.generate(config));
                    assertEquals("report failed", failure.getCause().getMessage());
                } else {
                    SchemeReporter.generate(config);
                }
                assertTrue(RecordingReporter.closed);
                try (var statement = observer.createStatement();
                     var sessions = statement.executeQuery("SELECT COUNT(*) FROM INFORMATION_SCHEMA.SESSIONS")) {
                    assertTrue(sessions.next());
                    assertEquals(1, sessions.getInt(1));
                }
            }
        }
    }

    public static class RecordingReporter implements Reportable, Closeable {
        static boolean closed;
        private boolean fail;

        public void setDatabase(org.jooq.meta.Database database) {}
        public void setGenerator(Generator generator) { fail = "fail".equals(generator.getTemplate()); }
        public void generate(SchemaDefinition schema) throws IOException {
            if (fail) throw new IOException("report failed");
        }
        public void close() { closed = true; }
    }
}
