package org.tinywind.schemereporter.util;

import org.jooq.tools.JooqLogger;
import org.jooq.tools.StringUtils;

import java.io.File;

public class FileUtils {
    private static final JooqLogger log = JooqLogger.getLogger(FileUtils.class);

    public static File getOutputFile(String outputDirectory, String fileExtension, String schema, String version) {
        final File file = getOutputFile(outputDirectory, fileExtension, schema, version, null);

        final File path = file.getParentFile();
        if (path != null) {
            if (path.mkdirs()) log.info("created path: " + path);
            else log.error("failed to create path: " + path);
        }

        return file;
    }

    private static File getOutputFile(String outputDirectory, String fileExtension, String schema, String version, Integer revise) {
        final File file = new File(outputDirectory, String.format("%s%s%s.%s",
                schema,
                !StringUtils.isEmpty(version) ? "-" + version : "",
                revise == null ? "" : " (" + revise + ")",
                fileExtension
        ));

        if (file.exists())
            return getOutputFile(outputDirectory, fileExtension, schema, version, revise == null ? 1 : revise + 1);
        
        return file;
    }
}
