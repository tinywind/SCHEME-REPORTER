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

import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.RankDir;
import guru.nidi.graphviz.attribute.Shape;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.Node;
import javafx.util.Pair;
import org.apache.jasper.JspC;
import org.apache.jasper.compiler.JspUtil;
import org.apache.jasper.runtime.HttpJspBase;
import org.apache.tools.ant.util.FileUtils;
import org.jooq.tools.JooqLogger;
import org.jooq.tools.StringUtils;
import org.jooq.util.*;
import org.tinywind.schemereporter.jaxb.Generator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static guru.nidi.graphviz.model.Factory.graph;
import static guru.nidi.graphviz.model.Factory.node;

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

    private Node createNode(String name) {
        return node(name).attr(Shape.RECTANGLE).attr(Color.BLACK)
                .attr("fontname", "Helvetica").attr("fontsize", 10).attr("fontcolor", "black")
                .attr("height", 0.2).attr("width", 0.4).attr("style", "filled");
    }

    private Node createReferNode(String name) {
        return createNode(name).attr("fillcolor", "white").attr("URL", "#table$" + name);
    }

    private Map<String, String> relationSvg(List<TableDefinition> tables) {
        final Map<String, String> relationSvg = new HashMap<>();
        for (TableDefinition table : tables) {
            final String cTable = table.getName();
            final Node[] cNode = {createNode(cTable).attr("weight", 8).attr("fillcolor", "grey75")};

            final Map<String, Node> refer = new HashMap<>();
            final Map<String, Node> referred = new HashMap<>();

            table.getColumns().forEach(column -> {
                column.getForeignKeys().forEach(fkey -> {
                    final String rTable = fkey.getReferencedTable().getName();
                    refer.putIfAbsent(rTable, createReferNode(rTable));
                });
                column.getUniqueKeys().forEach(ukey -> ukey.getForeignKeys().forEach(fkey -> {
                    final String rTable = fkey.getKeyTable().getName();
                    if (referred.get(rTable) == null && !cTable.equals(rTable))
                        referred.put(rTable, createReferNode(rTable).link(cNode[0]));
                }));
            });

            refer.values().stream().distinct().forEach(rNode -> cNode[0] = cNode[0].link(rNode));
            final Graph g = graph(cTable).directed().node(cNode[0]);
            referred.values().stream().distinct().forEach(g::node);

            relationSvg.put(cTable, Graphviz.fromGraph(g).createSvg());
        }
        return relationSvg;
    }

    private String totalRelationSvg(List<TableDefinition> tables) {
        final Map<String, Node> nodeMap = tables.stream().collect(Collectors.toMap(Definition::getName, table -> createReferNode(table.getName())));
        final Graph g = graph("totalRelationSvg").directed()
                .general().attr(RankDir.LEFT_TO_RIGHT);

        final List<Pair<String, String>> linkedList = new ArrayList<>();
        tables.forEach(table -> table.getColumns().forEach(column -> column.getForeignKeys().forEach(fkey -> {
            if (linkedList.contains(new Pair<>(table.getName(), fkey.getReferencedTable().getName()))) return;
            final Node linked = nodeMap.get(fkey.getReferencedTable().getName());
            nodeMap.put(table.getName(), nodeMap.get(table.getName()).link(linked));
            linkedList.add(new Pair<>(table.getName(), fkey.getReferencedTable().getName()));
        })));

        nodeMap.forEach((name, node) -> g.node(node));
        return Graphviz.fromGraph(g).createSvg();
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

        final List<TableDefinition> tables = database.getTables(schema);
        request.setAttribute("totalRelationSvg", totalRelationSvg(tables));
        request.setAttribute("relationSvg", relationSvg(tables));
        request.setAttribute("database", database);
        request.setAttribute("enums", database.getEnums(schema));
        request.setAttribute("sequences", database.getSequences(schema));
        request.setAttribute("tables", tables);

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