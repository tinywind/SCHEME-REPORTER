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
package org.tinywind.schemereporter.html;

import org.apache.jasper.JspC;
import org.apache.jasper.compiler.JspUtil;
import org.apache.jasper.runtime.HttpJspBase;
import org.apache.tools.ant.util.FileUtils;
import org.jooq.tools.JooqLogger;
import org.jooq.tools.StringUtils;
import org.jooq.util.Database;
import org.jooq.util.SchemaDefinition;
import org.jooq.util.SchemaVersionProvider;
import org.tinywind.schemereporter.jaxb.Generator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;

public class HtmlReporter {
    private static final JooqLogger log = JooqLogger.getLogger(HtmlReporter.class);

    static {
        System.setProperty("org.apache.el.parser.SKIP_IDENTIFIER_CHECK", "true");
    }

    private Database database;
    private Generator generator;

    public HtmlReporter(Database database, Generator generator) {
        this.database = database;
        this.generator = generator;
    }

    private HttpJspBase getCompiledJspBase(File jspTemplateFile, File tempDir) throws IOException, NoSuchMethodException, ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException {
        final String JSP_PACKAGE_NAME = "_compiled";

        final File compiledJspClassFile = jspCompile(jspTemplateFile.getPath(), JSP_PACKAGE_NAME, tempDir.getAbsolutePath());
        if (!compiledJspClassFile.exists()) {
            throw new RuntimeException("failed: JspCompile");
        }

        final URLClassLoader jspClassLoader = new URLClassLoader(new URL[]{tempDir.toURI().toURL()}, this.getClass().getClassLoader());
        final String jspClassName = (StringUtils.isEmpty(JSP_PACKAGE_NAME) ? "" : JSP_PACKAGE_NAME + ".") + getServletClassName(jspTemplateFile.getName());
        final Class<?> klass = Class.forName(jspClassName, true, jspClassLoader);
        return (HttpJspBase) klass.getConstructor().newInstance();
    }

    public final void generate(Database database) {
        this.database = database;
        this.database.setIncludeRelations(true);

        final HttpJspBase jsp;
        try {
            final String jspTemplate = generator.getJspTemplate();
            final File tempDir = Files.createTempDirectory("scheme-reporter").toFile();
            final File tempFile = Files.createTempFile(tempDir.toPath(), "template", ".jsp").toFile();
            final FileWriter writer = new FileWriter(tempFile);
            final char[] buffer = new char[1024 * 1024];
            final InputStreamReader reader = StringUtils.isEmpty(jspTemplate)
                    ? new InputStreamReader(getClass().getClassLoader().getResourceAsStream("asset/default.jsp"))
                    : new FileReader(jspTemplate);
            int read;
            while ((read = reader.read(buffer)) >= 0)
                writer.write(buffer, 0, read);
            writer.flush();

            try {
                reader.close();
            } catch (Exception ignored) {
            }
            try {
                writer.close();
            } catch (Exception ignored) {
            }

            jsp = getCompiledJspBase(tempFile, tempDir);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        for (SchemaDefinition schema : database.getSchemata()) {
            try {
                generate(schema, jsp);
            } catch (Exception e) {
                throw new RuntimeException("Error generating code for schema " + schema, e);
            }
        }
    }

    private void generate(SchemaDefinition schema, HttpJspBase jsp) throws Exception {
        final SchemaVersionProvider schemaVersionProvider = schema.getDatabase().getSchemaVersionProvider();
        final String version = schemaVersionProvider != null ? schemaVersionProvider.version(schema) : null;
        final File file = new File(generator.getOutputDirectory(), schema.getName() + (!StringUtils.isEmpty(version) ? "-" + version : "") + ".html");

        log.info("output file: " + file);
        final File path = file.getParentFile();
        if (path != null)
            path.mkdirs();

        final HttpServletRequest request = new SimpleServletRequest();
        final HttpServletResponse response = new SimpleServletResponse(file, "utf-8");

        request.setAttribute("database", database);
        request.setAttribute("enums", database.getEnums(schema));
        request.setAttribute("sequences", database.getSequences(schema));
        request.setAttribute("tables", database.getTables(schema));

        jsp.init(new SimpleServletConfig());
        jsp.service(request, response);
        response.getWriter().flush();
        response.getWriter().close();
    }

    private File jspCompile(String filePath, String packageName, String outputDir) {
        filePath = filePath.replaceAll("[\\\\]", "/");
        outputDir = outputDir.replaceAll("[\\\\]", "/");

        final JspC jspc = new JspC();
        final String URI_ROOT = outputDir;
        jspc.setUriroot(URI_ROOT);
        jspc.setOutputDir(outputDir);
        jspc.setCompile(true);

        log.info("Compiling " + filePath);
        jspc.setJspFiles(filePath);
        jspc.setPackage(packageName);

        jspc.execute();

        final File uriRoot = FileUtils.getFileUtils().resolveFile(jspc.getProject() == null ? null : jspc.getProject().getBaseDir(), URI_ROOT);
        File jspFile = new File(filePath);
        if (!jspFile.isAbsolute())
            jspFile = new File(uriRoot, filePath);

        final String jspAbsolutePath = jspFile.getAbsolutePath();
        final String uriRootAbsolutePath = uriRoot.getAbsolutePath();
        if (jspAbsolutePath.startsWith(uriRootAbsolutePath))
            filePath = jspAbsolutePath.substring(uriRootAbsolutePath.length());
        if (filePath.startsWith("." + File.separatorChar))
            filePath = filePath.substring(2);

        return new File(outputDir + "/" + packageName.replaceAll("[.]", "/"), getServletClassName(filePath).replaceAll("[.]", "/") + ".class");
    }

    private String getServletClassName(String jspUri) {
        jspUri = jspUri.replaceAll("\\\\", "/");
        String result = "";

        for (int index = jspUri.indexOf("/", 0), last = 0; index >= 0; index = jspUri.indexOf("/", index + 1)) {
            result += index == 0 ? "" : JspUtil.makeJavaIdentifier(jspUri.substring(last, index)) + ".";
            last = index + 1;
        }

        String fileName = jspUri;
        int index = jspUri.lastIndexOf("/");
        if (index >= 0)
            fileName = jspUri.substring(index + 1);
        int iSep = fileName.lastIndexOf(47) + 1;

        return result + JspUtil.makeJavaIdentifier(fileName.substring(iSep));
    }
}
