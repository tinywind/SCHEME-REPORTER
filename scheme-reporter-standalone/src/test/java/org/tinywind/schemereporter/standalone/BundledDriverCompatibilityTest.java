package org.tinywind.schemereporter.standalone;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.GraphvizJdkEngine;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.tinywind.schemereporter.SchemeReporter;
import org.tinywind.schemereporter.jaxb.Configuration;
import org.tinywind.schemereporter.jaxb.Database;
import org.tinywind.schemereporter.jaxb.Generator;
import org.tinywind.schemereporter.jaxb.Jdbc;

import java.io.File;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

public class BundledDriverCompatibilityTest {
    @Rule
    public TemporaryFolder temporary = new TemporaryFolder(new File("target"));

    @Test
    public void sixJava17DriversLoadAndRecognizeTheirJdbcUrls() throws Exception {
        String[][] drivers = {
                {"org.h2.Driver", "jdbc:h2:mem:driver_check"},
                {"org.hsqldb.jdbc.JDBCDriver", "jdbc:hsqldb:mem:driver_check"},
                {"org.firebirdsql.jdbc.FBDriver", "jdbc:firebirdsql://127.0.0.1:1/unused"},
                {"com.mysql.cj.jdbc.Driver", "jdbc:mysql://127.0.0.1:1/unused"},
                {"org.postgresql.Driver", "jdbc:postgresql://127.0.0.1:1/unused"},
                {"org.sqlite.JDBC", "jdbc:sqlite::memory:"}
        };
        for (String[] entry : drivers) {
            Driver driver = (Driver) Class.forName(entry[0]).getConstructor().newInstance();
            assertTrue(entry[0], driver.acceptsURL(entry[1]));
        }
    }

    @Test
    public void derbyCreatesARealInMemorySchemaReportOnJava21() throws Exception {
        assumeTrue("The bundled Derby 10.17 driver requires Java 21 or later", Runtime.version().feature() >= 21);
        String previousLog = System.getProperty("derby.stream.error.file");
        System.setProperty("derby.stream.error.file", temporary.newFile("derby.log").getAbsolutePath());
        String jdbcUrl = "jdbc:derby:memory:report_" + UUID.randomUUID().toString().replace("-", "");
        boolean created = false;
        try {
            Class.forName("org.apache.derby.iapi.jdbc.AutoloadedDriver");
            try (Connection connection = DriverManager.getConnection(jdbcUrl + ";create=true", "APP", "");
                 Statement statement = connection.createStatement()) {
                created = true;
                statement.execute("CREATE TABLE AUTHORS (ID BIGINT PRIMARY KEY, NAME VARCHAR(80))");
                statement.execute("CREATE TABLE BOOKS (ID BIGINT PRIMARY KEY, AUTHOR_ID BIGINT REFERENCES AUTHORS(ID))");
            }
            Graphviz.useEngine(new GraphvizJdkEngine());
            File output = temporary.newFolder("derby-report");
            Configuration configuration = new Configuration().withJdbc(new Jdbc().withDriverClass("org.apache.derby.iapi.jdbc.AutoloadedDriver"))
                    .withDatabase(new Database().withUrl(jdbcUrl).withUser("APP").withPassword("")
                            .withInputSchema("APP").withIncludes("AUTHORS|BOOKS").withExcludes(""))
                    .withGenerator(new Generator().withReporterClass("pdf").withTemplate("").withOutputDirectory(output.getAbsolutePath()));
            SchemeReporter.generate(configuration);
            try (PdfDocument pdf = new PdfDocument(new PdfReader(new File(output, "APP.pdf").getAbsolutePath()))) {
                assertTrue(pdf.getNumberOfPages() > 0);
                StringBuilder text = new StringBuilder();
                for (int page = 1; page <= pdf.getNumberOfPages(); page++) text.append(PdfTextExtractor.getTextFromPage(pdf.getPage(page)));
                assertTrue(text.toString(), text.toString().contains("AUTHORS"));
                assertTrue(text.toString(), text.toString().contains("BOOKS"));
                assertTrue(text.toString(), text.toString().contains("AUTHOR_ID"));
            }
        } finally {
            try {
                if (created) {
                    try {
                        DriverManager.getConnection(jdbcUrl + ";drop=true", "APP", "");
                        fail("Derby should report successful in-memory database removal with SQL state 08006");
                    } catch (SQLException exception) {
                        assertEquals("08006", exception.getSQLState());
                    }
                }
            } finally {
                if (previousLog == null) System.clearProperty("derby.stream.error.file");
                else System.setProperty("derby.stream.error.file", previousLog);
            }
        }
    }
}
