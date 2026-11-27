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

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlElement;
import org.apache.commons.io.input.CloseShieldInputStream;
import org.jooq.meta.Databases;
import org.jooq.meta.SchemaDefinition;
import org.jooq.meta.jaxb.CatalogMappingType;
import org.jooq.meta.jaxb.SchemaMappingType;
import org.jooq.tools.JooqLogger;
import org.jooq.tools.jdbc.JDBCUtils;
import org.tinywind.schemereporter.jaxb.Configuration;
import org.tinywind.schemereporter.jaxb.Database;
import org.tinywind.schemereporter.jaxb.Generator;
import org.tinywind.schemereporter.jaxb.Jdbc;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.SchemaFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Driver;
import java.util.*;

import static java.lang.System.exit;

public class SchemeReporter {
    private static final JooqLogger log = JooqLogger.getLogger(SchemeReporter.class);

    private final static String SCHEME_REPORTER_XSD = "scheme-reporter-0.1.xsd";
    private final static String REPO_XSD_URL = "https://raw.githubusercontent.com/tinywind/SCHEME-REPORTER/master/scheme-reporter/src/main/resources/xsd/";
    private final static String REPO_SCHEME_REPORTER_XSD = REPO_XSD_URL + SCHEME_REPORTER_XSD;

    public static void main(String[] args) {
        if (args.length < 1) {
            log.error("Usage : " + SchemeReporter.class.getName() + " <configuration-file>");
            exit(-1);
        }

        boolean failed = false;
        for (String arg : args) {
            InputStream in = SchemeReporter.class.getResourceAsStream(arg);
            try {
                if (in == null && !arg.startsWith("/")) in = SchemeReporter.class.getResourceAsStream("/" + arg);

                if (in == null && new File(arg).exists()) in = new FileInputStream(arg);

                if (in == null) {
                    log.error("Cannot find " + arg + " on classpath, or in directory " + new File(".").getCanonicalPath());
                    log.error("-----------");
                    log.error("Please be sure it is located");
                    log.error("  - on the classpath and qualified as a classpath location.");
                    log.error("  - in the local directory or at a global path in the file system.");
                    failed = true;
                    continue;
                }

                log.info("Initialising properties: " + arg);

                final Configuration configuration = load(in);
                generate(configuration);
            } catch (Exception e) {
                failed = true;
                log.error("Cannot read " + arg + ". Error : " + e.getMessage(), e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        if (failed) exit(1);
        log.info("complete");
    }

    private static boolean isNull(Object... objects) {
        for (Object o : objects)
            if (o == null) return true;
        return false;
    }

    public static boolean isCorrected(Configuration configuration) {
        if (configuration == null) return false;
        final Jdbc jdbc = configuration.getJdbc();
        final Database database = configuration.getDatabase();
        if (isNull(jdbc, database)) return false;

        return !isNull(jdbc.getDriverClass(), database.getUrl(), database.getUser(), database.getPassword())
                && !jdbc.getDriverClass().isBlank() && !database.getUrl().isBlank();
    }

    private static void setDefault(Object o, String fieldName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        final Field field = o.getClass().getDeclaredField(fieldName);
        final Method getter = o.getClass().getDeclaredMethod("get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1));
        final Method setter = o.getClass().getDeclaredMethod("set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1), getter.getReturnType());
        final Object getResult = getter.invoke(o);
        setter.invoke(o, getResult == null ? field.getAnnotation(XmlElement.class).defaultValue() : (getResult instanceof String ? getResult.toString().trim() : getResult));
    }

    private static void setDefault(Configuration configuration) throws NoSuchFieldException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (configuration.getGenerator() == null) configuration.setGenerator(new Generator());

        final Generator generator = configuration.getGenerator();
        setDefault(generator, "reporterClass");
        setDefault(generator, "outputDirectory");
        setDefault(generator, "template");

        if (configuration.getDatabase() == null) configuration.setDatabase(new Database());

        final Database database = configuration.getDatabase();
        setDefault(database, "includes");
        setDefault(database, "excludes");
        setDefault(database, "inputSchema");
    }

    @SuppressWarnings("unchecked")
    public static void generate(Configuration configuration) throws Exception {
        generate(configuration, config -> (Class<? extends Driver>) Class.forName(config.getJdbc().getDriverClass()));
    }

    public static void generate(Configuration configuration, JdbcClassExtractor jdbcClassExtractor) throws Exception {
        if (!isCorrected(configuration)) {
            throw new IllegalArgumentException("Configuration requires jdbc.driverClass, database.url, database.user and database.password");
        }
        setDefault(configuration);

        final Class<? extends Driver> driverClass = jdbcClassExtractor.extract(configuration);
        final Properties properties = new Properties();
        final Database databaseConfig = configuration.getDatabase();
        properties.put("user", databaseConfig.getUser());
        properties.put("password", databaseConfig.getPassword());

        try (Connection connection = driverClass.getDeclaredConstructor().newInstance().connect(databaseConfig.getUrl(), properties);
             org.jooq.meta.Database database = Databases.databaseClass(JDBCUtils.dialect(databaseConfig.getUrl())).getDeclaredConstructor().newInstance()) {
            if (connection == null) throw new IllegalArgumentException("JDBC driver does not accept the configured URL");
            database.setConnection(connection);
            generate(configuration, database);
        }
    }

    private static void generate(Configuration configuration, org.jooq.meta.Database database) throws Exception {
        final Database databaseConfig = configuration.getDatabase();

        final CatalogMappingType catalog = new CatalogMappingType();
        catalog.setInputCatalog("");
        catalog.setOutputCatalog("");
        catalog.setOutputCatalogToDefault(false);
        catalog.setSchemata(Collections.singletonList(new SchemaMappingType()));

        database.setConfiguredCatalogs(Collections.singletonList(catalog));
        database.setConfiguredSchemata(new ArrayList<>());
        database.setConfiguredEnumTypes(new ArrayList<>());
        database.setIncludes(new String[]{databaseConfig.getIncludes()});
        database.setExcludes(new String[]{databaseConfig.getExcludes()});
        database.setIncludeRelations(true);

        log.info("----------------------------------------------------------");
        log.info("Database parameters");
        log.info("----------------------------------------------------------");
        log.info("  dialect", database.getDialect());
        log.info("  includes", Arrays.asList(database.getIncludes()));
        log.info("  excludes", Arrays.asList(database.getExcludes()));
        log.info("  includeExcludeColumns", database.getIncludeExcludeColumns());
        log.info("----------------------------------------------------------");
        log.info("Reporter parameters");
        log.info("----------------------------------------------------------");
        log.info("----------------------------------------------------------");

        final Generator generator = configuration.getGenerator();
        if (generator.getReporterClass().equalsIgnoreCase("html"))
            generator.setReporterClass("org.tinywind.schemereporter.html.HtmlReporter");
        else if (generator.getReporterClass().equalsIgnoreCase("pdf"))
            generator.setReporterClass("org.tinywind.schemereporter.pdf.PdfReporter");
        else if (generator.getReporterClass().equalsIgnoreCase("excel"))
            generator.setReporterClass("org.tinywind.schemereporter.excel.ExcelReporter");
        else if (generator.getReporterClass().equalsIgnoreCase("docx"))
            generator.setReporterClass("org.tinywind.schemereporter.docx.DocxReporter");

        log.info("Reporter class", generator.getReporterClass());

        @SuppressWarnings("unchecked") final Class<? extends Reportable> reporterClass = (Class<? extends Reportable>) Class.forName(generator.getReporterClass());
        final Reportable reporter = reporterClass.getConstructor().newInstance();
        try (Closeable resource = reporter instanceof Closeable closeable ? closeable : null) {
            reporter.setDatabase(database);
            reporter.setGenerator(generator);

            final List<String> inputSchemaNames = databaseConfig.getInputSchema() != null ? Arrays.asList(databaseConfig.getInputSchema().toLowerCase().split("[|]")) : null;
            for (SchemaDefinition schemaDefinition : database.getSchemata()) {
                if (inputSchemaNames != null && !inputSchemaNames.contains(schemaDefinition.getName().toLowerCase()) && !inputSchemaNames.contains(schemaDefinition.getOutputName().toLowerCase()))
                    continue;

                try {
                    reporter.generate(schemaDefinition);
                } catch (Exception e) {
                    throw new RuntimeException("Error generating code for schema " + schemaDefinition, e);
                }
            }
        }
    }

    public static Configuration load(InputStream in) throws IOException {
        try {
            final DocumentBuilderFactory parser = DocumentBuilderFactory.newDefaultNSInstance();
            parser.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            parser.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            parser.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            final Document document = parser.newDocumentBuilder().parse(CloseShieldInputStream.wrap(in));
            final Element root = document.getDocumentElement();
            if (!REPO_SCHEME_REPORTER_XSD.equals(root.getNamespaceURI())) {
                useConfigurationNamespace(document, root.getNamespaceURI());
            }

            final SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            sf.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            sf.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            final Unmarshaller unmarshaller = JAXBContext.newInstance(Configuration.class).createUnmarshaller();
            unmarshaller.setSchema(sf.newSchema(SchemeReporter.class.getResource("/xsd/" + SCHEME_REPORTER_XSD)));
            unmarshaller.setEventHandler(event -> false);
            return (Configuration) unmarshaller.unmarshal(document);
        } catch (Exception e) {
            throw new IOException("Invalid scheme reporter configuration", e);
        }
    }

    private static void useConfigurationNamespace(Document document, String originalNamespace) {
        NodeList matches = document.getElementsByTagNameNS(originalNamespace, "*");
        // Renaming nodes changes the live NodeList.
        List<Element> elements = new ArrayList<>(matches.getLength());
        for (int i = 0; i < matches.getLength(); i++) elements.add((Element) matches.item(i));
        for (Element element : elements) {
            document.renameNode(element, REPO_SCHEME_REPORTER_XSD, element.getNodeName());
        }
    }
}
