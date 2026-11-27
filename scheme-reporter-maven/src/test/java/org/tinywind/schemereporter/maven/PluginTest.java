package org.tinywind.schemereporter.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Test;

import static org.junit.Assert.*;

public class PluginTest {
    @Test
    public void missingConfigurationFailsTheGoalWithTheCause() {
        MojoExecutionException failure = assertThrows(MojoExecutionException.class, () -> new Plugin().execute());
        assertTrue(failure.getCause() instanceof IllegalArgumentException);
    }

    @Test
    public void skipDoesNotRequireAConfiguration() throws Exception {
        Plugin plugin = new Plugin();
        var skip = Plugin.class.getDeclaredField("skip");
        skip.setAccessible(true);
        skip.setBoolean(plugin, true);
        plugin.execute();
    }
}
