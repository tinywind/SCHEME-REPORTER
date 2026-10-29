package org.tinywind.schemereporter;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.GraphvizJdkEngine;
import jakarta.xml.bind.JAXB;
import org.apache.poi.ss.usermodel.*;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.tinywind.schemereporter.jaxb.Configuration;
import org.tinywind.schemereporter.jaxb.Database;
import org.tinywind.schemereporter.jaxb.Generator;
import org.tinywind.schemereporter.jaxb.Jdbc;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.UUID;
import java.util.zip.ZipFile;

import static org.junit.Assert.*;

public class ReportExportContractTest {
    @Rule
    public TemporaryFolder temporary = new TemporaryFolder(new File("target"));
    private String jdbcUrl;

    @BeforeClass
    public static void useJavaGraphvizEngine() {
        Graphviz.useEngine(new GraphvizJdkEngine());
    }

    @Before
    public void createRelatedTables() throws Exception {
        jdbcUrl = "jdbc:h2:mem:report_" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1";
        try (Connection connection = DriverManager.getConnection(jdbcUrl, "sa", "");
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE authors (id BIGINT PRIMARY KEY, name VARCHAR(80) NOT NULL)");
            statement.execute("CREATE TABLE books (id BIGINT PRIMARY KEY, author_id BIGINT NOT NULL, title VARCHAR(120), CONSTRAINT book_author FOREIGN KEY(author_id) REFERENCES authors(id))");
            statement.execute("COMMENT ON COLUMN books.title IS 'Title & note'");
        }
    }

    @After
    public void closeMemoryDatabase() throws Exception {
        if (jdbcUrl != null) {
            try (Connection connection = DriverManager.getConnection(jdbcUrl, "sa", "");
                 Statement statement = connection.createStatement()) {
                statement.execute("SHUTDOWN");
            }
        }
    }

    @Test
    public void htmlContainsRealSchemaAndRelationshipSvg() throws Exception {
        Path output = generate("html", null);
        String html = Files.readString(output, StandardCharsets.UTF_8);
        assertTrue(html, html.contains("AUTHORS"));
        assertTrue(html, html.contains("BOOKS"));
        assertTrue(html, html.contains("AUTHOR_ID"));
        assertTrue(html, html.contains("<svg"));
        assertTrue(html, html.contains("Title &amp; note"));
    }

    @Test
    public void pdfContainsReadableTableAndColumnText() throws Exception {
        try (PdfDocument pdf = new PdfDocument(new PdfReader(generate("pdf", null).toString()))) {
            assertTrue(pdf.getNumberOfPages() > 0);
            StringBuilder text = new StringBuilder();
            for (int page = 1; page <= pdf.getNumberOfPages(); page++) {
                text.append(PdfTextExtractor.getTextFromPage(pdf.getPage(page)));
            }
            assertTrue(text.toString(), text.toString().contains("AUTHORS"));
            assertTrue(text.toString(), text.toString().contains("BOOKS"));
            assertTrue(text.toString(), text.toString().contains("AUTHOR_ID"));
        }
    }

    @Test
    public void excelContainsForeignKeyAndColumnDescription() throws Exception {
        try (Workbook workbook = WorkbookFactory.create(generate("excel", null).toFile())) {
            Sheet tables = workbook.getSheet("tables");
            assertNotNull(tables);
            StringBuilder cells = new StringBuilder();
            for (Row row : tables) for (Cell cell : row) cells.append(cell.toString()).append('\n');
            assertTrue(cells.toString(), cells.toString().contains("AUTHORS"));
            assertTrue(cells.toString(), cells.toString().contains("BOOKS"));
            assertTrue(cells.toString(), cells.toString().contains("[AUTHORS] ID"));
            assertTrue(cells.toString(), cells.toString().contains("Title & note"));
        }
    }

    @Test
    public void docxContainsTableTextAndEmbeddedRelationshipImages() throws Exception {
        try (ZipFile document = new ZipFile(generate("docx", null).toFile())) {
            String xml;
            try (InputStream input = document.getInputStream(document.getEntry("word/document.xml"))) {
                xml = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            }
            assertTrue(xml, xml.contains("AUTHORS"));
            assertTrue(xml, xml.contains("BOOKS"));
            assertTrue(xml, xml.contains("AUTHOR_ID"));
            assertTrue(xml, xml.contains("Title &amp; note"));
            assertTrue(document.stream().anyMatch(entry -> entry.getName().startsWith("word/media/")));
        }
    }

    @Test
    public void customHtmlTemplateReceivesActualMetadata() throws Exception {
        Path template = temporary.newFile("custom.html").toPath();
        Files.writeString(template, "<html xmlns:th=\"http://www.thymeleaf.org\"><body><p th:each=\"table : ${tables}\" th:text=\"${table.name}\"></p></body></html>", StandardCharsets.UTF_8);
        String html = Files.readString(generate("html", template), StandardCharsets.UTF_8);
        assertTrue(html, html.contains("<p>AUTHORS</p>"));
        assertTrue(html, html.contains("<p>BOOKS</p>"));
        assertFalse(html, html.contains("th:text"));
    }

    @Test
    public void xmlRoundTripPreservesTheConfiguredReportContract() throws Exception {
        Configuration configuration = configuration("pdf", temporary.newFolder("xml").toPath(), null);
        StringWriter xml = new StringWriter();
        JAXB.marshal(configuration, xml);
        Configuration loaded = SchemeReporter.load(new ByteArrayInputStream(xml.toString().getBytes(StandardCharsets.UTF_8)));
        assertEquals(jdbcUrl, loaded.getDatabase().getUrl());
        assertEquals("PUBLIC", loaded.getDatabase().getInputSchema());
        assertEquals("pdf", loaded.getGenerator().getReporterClass());
        assertEquals("org.h2.Driver", loaded.getJdbc().getDriverClass());
    }

    private Path generate(String type, Path template) throws Exception {
        Path directory = temporary.newFolder(type).toPath();
        SchemeReporter.generate(configuration(type, directory, template));
        Path output = directory.resolve("PUBLIC." + ("excel".equals(type) ? "xlsx" : type));
        assertTrue("Missing " + output, Files.isRegularFile(output));
        return output;
    }

    private Configuration configuration(String type, Path directory, Path template) {
        return new Configuration().withJdbc(new Jdbc().withDriverClass("org.h2.Driver"))
                .withDatabase(new Database().withUrl(jdbcUrl).withUser("sa").withPassword("")
                        .withInputSchema("PUBLIC").withIncludes("AUTHORS|BOOKS").withExcludes(""))
                .withGenerator(new Generator().withReporterClass(type).withOutputDirectory(directory.toString())
                        .withTemplate(template == null ? "" : template.toString()));
    }
}
