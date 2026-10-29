package org.tinywind;

import org.junit.Test;

import java.io.File;
import java.io.ObjectInputFilter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ShadedGuavaSerializationTest {
    @Test
    public void buildFilterRejectsShadedGuavaAllocationPayloads() throws Exception {
        String filter = System.getProperty("jdk.serialFilter");
        assertNotNull("The Gradle build must protect Java deserialization", filter);
        assertNotNull("The test JVM filter must be active before deserialization", ObjectInputFilter.Config.getSerialFilter());
        for (String type : new String[]{"CompactHashMap", "CompactHashSet", "MapMakerInternalMap"}) {
            assertEquals(type + " control", "VULNERABLE_ALLOCATION", probe(type, null));
            assertEquals(type + " protected", "FILTER_REJECTED", probe(type, filter));
        }
    }

    private String probe(String type, String filter) throws Exception {
        String pluginJar = System.getProperty("schemeReporterKotlinPluginJar");
        assertNotNull("The actual Kotlin build plugin JAR must be supplied", pluginJar);
        String testClasses = new File(GuavaPayloadProbe.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getAbsolutePath();
        List<String> command = new ArrayList<>();
        command.add(Path.of(System.getProperty("java.home"), "bin", "java").toString());
        command.add("-Xmx32m");
        command.add("-Djava.io.tmpdir=" + System.getProperty("java.io.tmpdir"));
        if (filter != null) command.add("-Djdk.serialFilter=" + filter);
        command.add("-cp");
        command.add(testClasses + File.pathSeparator + pluginJar);
        command.add(GuavaPayloadProbe.class.getName());
        command.add(type);
        ProcessBuilder builder = new ProcessBuilder(command).redirectErrorStream(true);
        builder.environment().remove("JAVA_TOOL_OPTIONS");
        builder.environment().remove("JDK_JAVA_OPTIONS");
        Process process = builder.start();
        try {
            assertTrue(type + " probe timed out", process.waitFor(20, TimeUnit.SECONDS));
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
            assertEquals(output, 0, process.exitValue());
            return output;
        } finally {
            if (process.isAlive()) process.destroyForcibly();
        }
    }
}
