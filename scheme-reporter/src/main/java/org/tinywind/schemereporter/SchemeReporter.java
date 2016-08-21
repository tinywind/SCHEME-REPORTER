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

import org.jooq.tools.JooqLogger;
import org.jooq.tools.jdbc.JDBCUtils;
import org.jooq.util.Databases;
import org.jooq.util.jaxb.EnumType;
import org.jooq.util.jaxb.Schema;
import org.tinywind.schemereporter.html.HtmlReporter;
import org.tinywind.schemereporter.jaxb.Configuration;
import org.tinywind.schemereporter.jaxb.Database;
import org.tinywind.schemereporter.jaxb.Generator;
import org.tinywind.schemereporter.jaxb.Jdbc;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.validation.SchemaFactory;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

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

        for (String arg : args) {
            InputStream in = SchemeReporter.class.getResourceAsStream(arg);
            try {
                if (in == null && !arg.startsWith("/"))
                    in = SchemeReporter.class.getResourceAsStream("/" + arg);

                if (in == null && new File(arg).exists())
                    in = new FileInputStream(new File(arg));

                if (in == null) {
                    log.error("Cannot find " + arg + " on classpath, or in directory " + new File(".").getCanonicalPath());
                    log.error("-----------");
                    log.error("Please be sure it is located");
                    log.error("  - on the classpath and qualified as a classpath location.");
                    log.error("  - in the local directory or at a global path in the file system.");
                    continue;
                }

                log.info("Initialising properties: " + arg);

                final Configuration configuration = load(in);
                generate(configuration);
            } catch (Exception e) {
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
        log.info("complete");
    }

    private static boolean isNull(Object... objects) {
        for (Object o : objects)
            if (o == null)
                return true;
        return false;
    }

    private static boolean isCorrected(Configuration configuration) {
        final Jdbc jdbc = configuration.getJdbc();
        final Database database = configuration.getDatabase();
        if (isNull(jdbc, database))
            return false;

        if (isNull(jdbc.getDriverClass(), database.getUrl(), database.getUser(), database.getPassword()))
            return false;

        return true;
    }

    private static void setDefault(Object o, String fieldName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        final Field field = o.getClass().getDeclaredField(fieldName);
        final Method getter = o.getClass().getDeclaredMethod("get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1));
        final Method setter = o.getClass().getDeclaredMethod("set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1), getter.getReturnType());
        final Object getResult = getter.invoke(o);
        setter.invoke(o, getResult == null ? field.getAnnotation(XmlElement.class).defaultValue() : (getResult instanceof String ? getResult.toString().trim() : getResult));
    }

    private static void setDefault(Configuration configuration) throws NoSuchFieldException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (configuration.getGenerator() == null)
            configuration.setGenerator(new Generator());

        final Generator generator = configuration.getGenerator();
        setDefault(generator, "outputDirectory");
        setDefault(generator, "jspTemplate");

        if (configuration.getDatabase() == null)
            configuration.setDatabase(new Database());

        final Database database = configuration.getDatabase();
        setDefault(database, "includes");
        setDefault(database, "excludes");
        setDefault(database, "inputSchema");
    }

    public static void generate(Configuration configuration) throws Exception {
        if (!isCorrected(configuration)) {
            log.error("Incorrect xml");
            return;
        }
        try {
            setDefault(configuration);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        final Class<? extends Driver> driverClass = (Class<? extends Driver>) Class.forName(configuration.getJdbc().getDriverClass());
        final Properties properties = new Properties();
        final Database databaseConfig = configuration.getDatabase();
        properties.put("user", databaseConfig.getUser());
        properties.put("password", databaseConfig.getPassword());

        final Connection connection = driverClass.newInstance().connect(databaseConfig.getUrl(), properties);
        final org.jooq.util.Database database = Databases.databaseClass(JDBCUtils.dialect(databaseConfig.getUrl())).newInstance();
        database.setConnection(connection);
        database.setIncludes(new String[]{databaseConfig.getIncludes()});
        database.setExcludes(new String[]{databaseConfig.getExcludes()});

        final List<Schema> schemata = new ArrayList<>();
        final Schema schema = new Schema();
        schema.setInputSchema(databaseConfig.getInputSchema());
        schemata.add(schema);
        database.setConfiguredSchemata(schemata);

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

        database.setConfiguredEnumTypes(new ArrayList<>());
        new HtmlReporter(database, configuration.getGenerator()).generate(database);
    }

    private static Configuration load(InputStream in) throws IOException {
        final byte[] buffer = new byte[1000 * 1000];
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int len; (len = in.read(buffer)) >= 0; )
            out.write(buffer, 0, len);

        final String xml = out.toString()
                .replaceAll("<(\\w+:)?configuration\\s+xmlns(:\\w+)?=\"[^\"]*\"[^>]*>", "<$1configuration xmlns$2=\"" + REPO_SCHEME_REPORTER_XSD + "\">")
                .replace("<configuration>", "<configuration xmlns=\"" + REPO_SCHEME_REPORTER_XSD + "\">");
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            final SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            final JAXBContext ctx = JAXBContext.newInstance(Configuration.class);
            final Unmarshaller unmarshaller = ctx.createUnmarshaller();
            unmarshaller.setSchema(sf.newSchema(SchemeReporter.class.getResource("/xsd/" + SCHEME_REPORTER_XSD)));
            unmarshaller.setEventHandler(event -> true);
            return (Configuration) unmarshaller.unmarshal(new StringReader(xml));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
