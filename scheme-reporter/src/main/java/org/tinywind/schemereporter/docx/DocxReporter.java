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
package org.tinywind.schemereporter.docx;

import org.docx4j.XmlUtils;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.jaxb.Context;
import org.docx4j.model.table.TblFactory;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.toc.TocGenerator;
import org.docx4j.wml.*;
import org.jooq.meta.*;
import org.jooq.tools.JooqLogger;
import org.jooq.tools.StringUtils;
import org.tinywind.schemereporter.Reportable;
import org.tinywind.schemereporter.jaxb.Generator;
import org.tinywind.schemereporter.util.FileUtils;
import org.tinywind.schemereporter.util.TableImage;

import java.io.File;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static org.tinywind.schemereporter.util.TableImage.pngBytesFromSvg;

public class DocxReporter implements Reportable {
    private static final JooqLogger log = JooqLogger.getLogger(DocxReporter.class);
    private static final ObjectFactory factory = Context.getWmlObjectFactory();
    private static final String initialNumbering = "<w:numbering xmlns:ve=\"http://schemas.openxmlformats.org/markup-compatibility/2006\" xmlns:o=\"urn:schemas-microsoft-com:office:office\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" xmlns:m=\"http://schemas.openxmlformats.org/officeDocument/2006/math\" xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\" xmlns:w10=\"urn:schemas-microsoft-com:office:word\" xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" xmlns:wne=\"http://schemas.microsoft.com/office/word/2006/wordml\">" + "<w:abstractNum w:abstractNumId=\"0\">" + "<w:nsid w:val=\"2DD860C0\"/>" + "<w:multiLevelType w:val=\"multilevel\"/>" + "<w:tmpl w:val=\"0409001D\"/>" + "<w:lvl w:ilvl=\"0\">" + "<w:start w:val=\"1\"/>" + "<w:numFmt w:val=\"decimal\"/>" + "<w:lvlText w:val=\"(%1)\"/>" + "<w:lvlJc w:val=\"left\"/>" + "<w:pPr>" + "<w:ind w:left=\"720\" w:hanging=\"360\"/>" + "</w:pPr>" + "</w:lvl>" + "</w:abstractNum>" + "<w:num w:numId=\"1\">" + "<w:abstractNumId w:val=\"0\"/>" + "</w:num>" + "</w:numbering>";
    private Database database;
    private Generator generator;
    private int imageId = 0;
    private int bookmarkId = 0;

    @Override
    public void setDatabase(Database database) {
        this.database = database;
    }

    @Override
    public void setGenerator(Generator generator) {
        this.generator = generator;
    }

    @Override
    public final void generate(SchemaDefinition schema) throws Exception {
        final SchemaVersionProvider schemaVersionProvider = schema.getDatabase().getSchemaVersionProvider();
        final String version = schemaVersionProvider != null ? schemaVersionProvider.version(schema) : null;
        final File file = FileUtils.getOutputFile(generator.getOutputDirectory(), "docx", schema.getName(), version);

        final WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
        final MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();

        final NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();
        wordMLPackage.getMainDocumentPart().addTargetPart(ndp);
        ndp.setJaxbElement((Numbering) XmlUtils.unmarshalString(initialNumbering));

        final String TOC_STYLE_MASK = "TOC%s";
        for (int i = 1; i < 10; i++)
            documentPart.getPropertyResolver().activateStyle(String.format(TOC_STYLE_MASK, i));

        final List<EnumDefinition> enums = database.getEnums(schema);
        final List<TableDefinition> tables = database.getTables(schema);
        final String totalRelationSvg = TableImage.totalRelationSvg(tables);
        final Map<String, String> relationSvg = TableImage.relationSvg(tables);

        documentPart.addParagraphOfText("");
        documentPart.addStyledParagraphOfText("Heading1", "Enum Definition");

        long numberingId = 1;
        for (EnumDefinition e : enums) {
            documentPart.addParagraphOfText("");
            numberingId = ndp.restart(numberingId, 0, 1);
            documentPart.addStyledParagraphOfText("Heading2", e.getName());
            if (!StringUtils.isEmpty(e.getComment())) documentPart.addStyledParagraphOfText("Normal", e.getComment());
            for (String literal : e.getLiterals())
                documentPart.addObject(numberedText(numberingId, 0, literal));
        }

        documentPart.addParagraphOfText("");
        documentPart.addStyledParagraphOfText("Heading1", "Table Definition");
        documentPart.addObject(newImage(wordMLPackage, pngBytesFromSvg(totalRelationSvg)));

        for (TableDefinition e : tables) {
            documentPart.addParagraphOfText("");
            final P tableTitleP = documentPart.addStyledParagraphOfText("Heading2", e.getName());
            attachBookmark("table$" + e.getName(), tableTitleP);
            documentPart.addStyledParagraphOfText("Normal", e.getComment());
            documentPart.addObject(newImage(wordMLPackage, pngBytesFromSvg(relationSvg.get(e.getName()))));
            documentPart.addObject(tableDefinition(wordMLPackage, e));
            documentPart.addParagraphOfText("");
            if (!e.getKeys().isEmpty()) {
                documentPart.addObject(tableUniqueKey(wordMLPackage, e));
                documentPart.addParagraphOfText("");
            }
            documentPart.addObject(tableColumnComments(wordMLPackage, e));
        }

        new TocGenerator(wordMLPackage).generateToc(0, " TOC \\o \"1-3\" \\h \\z \\u ", true);
        wordMLPackage.save(file);
    }

    private void clearTableContents(Tbl table) {
        for (Object o : table.getContent()) {
            final Tr tr = (Tr) o;
            for (Object o1 : tr.getContent()) {
                final Tc tc = ((Tc) o1);
                final CTVerticalJc verticalJc = new CTVerticalJc();
                verticalJc.setVal(STVerticalJc.CENTER);
                tc.getTcPr().setVAlign(verticalJc);
                tc.getContent().clear();
            }
        }
    }

    private Tbl tableDefinition(WordprocessingMLPackage wordMLPackage, TableDefinition e) {
        final int INDEX_COLUMN_COLUMN_NAME = 0;
        final int INDEX_COLUMN_TYPE = 1;
        final int INDEX_COLUMN_NULLABLE = 2;
        final int INDEX_COLUMN_PKEY = 3;
        final int INDEX_COLUMN_DEFAULTED = 4;
        final int INDEX_COLUMN_REFERRED = 5;
        final int INDEX_COLUMN_REFER = 6;
        final int COLUMN_SIZE = INDEX_COLUMN_REFER + 1;

        final int writableWidthTwips = wordMLPackage.getDocumentModel().getSections().get(0).getPageDimensions().getWritableWidthTwips();
        final Tbl table = TblFactory.createTable(e.getColumns().size() + 1, COLUMN_SIZE, writableWidthTwips / COLUMN_SIZE);
        clearTableContents(table);
        setTableWidthFull(table);

        final Tr header = (Tr) table.getContent().get(0);
        tableHeaderCell(header, INDEX_COLUMN_COLUMN_NAME, 15).getContent().add(tableText("COLUMN", JcEnumeration.CENTER));
        tableHeaderCell(header, INDEX_COLUMN_TYPE, 15).getContent().add(tableText("TYPE", JcEnumeration.CENTER));
        tableHeaderCell(header, INDEX_COLUMN_NULLABLE, 10).getContent().add(tableText("NULLABLE", JcEnumeration.CENTER));
        tableHeaderCell(header, INDEX_COLUMN_PKEY, 10).getContent().add(tableText("PKEY", JcEnumeration.CENTER));
        tableHeaderCell(header, INDEX_COLUMN_DEFAULTED, 10).getContent().add(tableText("DEFAULTED", JcEnumeration.CENTER));
        tableHeaderCell(header, INDEX_COLUMN_REFERRED, 20).getContent().add(tableText("REFERRED", JcEnumeration.CENTER));
        tableHeaderCell(header, INDEX_COLUMN_REFER, 20).getContent().add(tableText("REFER", JcEnumeration.CENTER));

        for (int i = 0; i < e.getColumns().size(); i++) {
            final ColumnDefinition column = e.getColumns().get(i);
            final String type = column.getType().getType();
            final String typeName = type.equalsIgnoreCase("USER-DEFINED") ? column.getType().getUserType() : type + (column.getType().getLength() != 0 ? "(" + column.getType().getLength() + ")" : "");
            final Tr tr = (Tr) table.getContent().get(i + 1);

            tableCell(tr, INDEX_COLUMN_COLUMN_NAME).getContent().add(tableText(column.getName(), JcEnumeration.CENTER));
            tableCell(tr, INDEX_COLUMN_TYPE).getContent().add(tableText(typeName, JcEnumeration.CENTER));
            tableCell(tr, INDEX_COLUMN_NULLABLE).getContent().add(tableText(column.getType().isNullable() ? "●" : "", JcEnumeration.CENTER));
            tableCell(tr, INDEX_COLUMN_PKEY).getContent().add(tableText(column.getPrimaryKey() != null ? "●" : "", JcEnumeration.CENTER));
            tableCell(tr, INDEX_COLUMN_DEFAULTED).getContent().add(tableText(column.getType().isDefaulted() ? "●" : "", JcEnumeration.CENTER));

            if (column.getKeys().isEmpty() || column.getKeys().stream().allMatch(pkey -> pkey.getForeignKeys().isEmpty())) {
                tableCell(tr, INDEX_COLUMN_REFERRED).getContent().add(tableText(""));
            } else {
                for (UniqueKeyDefinition key : column.getKeys()) {
                    writeKeyDescriptions(tableCell(tr, INDEX_COLUMN_REFERRED), key.getForeignKeys(), false);
                }
            }

            if (column.getForeignKeys().isEmpty()) {
                ((Tc) tr.getContent().get(INDEX_COLUMN_REFER)).getContent().add(tableText(""));
            } else {
                writeKeyDescriptions(tableCell(tr, INDEX_COLUMN_REFER), column.getForeignKeys(), true);
            }
        }

        return table;
    }

    private Tbl tableColumnComments(WordprocessingMLPackage wordMLPackage, TableDefinition e) {
        final int INDEX_COLUMN_COLUMN_NAME = 0;
        final int INDEX_COLUMN_DESCRIPTION = 1;
        final int COLUMN_SIZE = INDEX_COLUMN_DESCRIPTION + 1;

        final int writableWidthTwips = wordMLPackage.getDocumentModel().getSections().get(0).getPageDimensions().getWritableWidthTwips();
        final Tbl table = TblFactory.createTable(e.getColumns().size() + 1, COLUMN_SIZE, writableWidthTwips / COLUMN_SIZE);
        clearTableContents(table);
        setTableWidthFull(table);

        final Tr header = (Tr) table.getContent().get(0);
        tableHeaderCell(header, INDEX_COLUMN_COLUMN_NAME, 15).getContent().add(tableText("COLUMN", JcEnumeration.CENTER));
        tableHeaderCell(header, INDEX_COLUMN_DESCRIPTION, 85).getContent().add(tableText("DESCRIPTION", JcEnumeration.CENTER));

        for (int i = 0; i < e.getColumns().size(); i++) {
            final ColumnDefinition column = e.getColumns().get(i);
            final Tr tr = (Tr) table.getContent().get(i + 1);

            tableCell(tr, INDEX_COLUMN_COLUMN_NAME).getContent().add(tableText(column.getName(), JcEnumeration.CENTER));
            tableCell(tr, INDEX_COLUMN_DESCRIPTION).getContent().add(tableText(column.getComment() != null ? column.getComment() : ""));
        }

        return table;
    }

    private Tbl tableUniqueKey(WordprocessingMLPackage wordMLPackage, TableDefinition e) {
        final int INDEX_COLUMN_KEY_NAME = 0;
        final int INDEX_COLUMN_COLUMNS = 1;
        final int INDEX_COLUMN_DESCRIPTION = 2;
        final int COLUMN_SIZE = INDEX_COLUMN_DESCRIPTION + 1;

        final int writableWidthTwips = wordMLPackage.getDocumentModel().getSections().get(0).getPageDimensions().getWritableWidthTwips();
        final Tbl table = TblFactory.createTable(e.getKeys().size() + 1, COLUMN_SIZE, writableWidthTwips / COLUMN_SIZE);
        clearTableContents(table);
        setTableWidthFull(table);

        final Tr header = (Tr) table.getContent().get(0);

        tableHeaderCell(header, INDEX_COLUMN_KEY_NAME, 15).getContent().add(tableText("UNIQUE KEY", JcEnumeration.CENTER));
        tableHeaderCell(header, INDEX_COLUMN_COLUMNS, 15).getContent().add(tableText("COLUMNS", JcEnumeration.CENTER));
        tableHeaderCell(header, INDEX_COLUMN_DESCRIPTION, 70).getContent().add(tableText("DESCRIPTION", JcEnumeration.CENTER));

        for (int i = 0; i < e.getKeys().size(); i++) {
            final UniqueKeyDefinition ukey = e.getKeys().get(i);
            final Tr tr = (Tr) table.getContent().get(i + 1);
            final StringBuilder columnString = new StringBuilder();
            for (ColumnDefinition column : ukey.getKeyColumns()) columnString.append(column.getName()).append(" ");

            final P keyNameParagraph = tableText(ukey.getName(), JcEnumeration.CENTER);
            attachBookmark("key$" + ukey.getName(), keyNameParagraph);

            tableCell(tr, INDEX_COLUMN_KEY_NAME).getContent().add(keyNameParagraph);
            tableCell(tr, INDEX_COLUMN_COLUMNS).getContent().add(tableText(columnString.toString(), JcEnumeration.CENTER));
            tableCell(tr, INDEX_COLUMN_DESCRIPTION).getContent().add(tableText(ukey.getComment() != null ? ukey.getComment() : ""));
        }

        return table;
    }

    private void writeKeyDescriptions(Tc tc, List<ForeignKeyDefinition> foreignKeys, boolean referenced) {
        for (ForeignKeyDefinition fkey : foreignKeys) {
            final StringBuilder text = new StringBuilder("[" + (referenced ? fkey.getReferencedTable().getName() : fkey.getKeyTable().getName()) + "]");
            if (referenced)
                for (ColumnDefinition column : fkey.getReferencedColumns()) text.append(" ").append(column.getName());
            else for (ColumnDefinition column : fkey.getKeyColumns()) text.append(" ").append(column.getName());

            if (referenced) {
                final P p = tableText("");
                final P.Hyperlink hyperlink = MainDocumentPart.hyperlinkToBookmark(fkey.getReferencedKey().getName(), text.toString());
                setTextSize(hyperlink);
                p.getContent().add(hyperlink);
                tc.getContent().add(p);
            } else {
                tc.getContent().add(tableText(text.toString()));
            }
        }
    }

    private Tc tableHeaderCell(Tr row, int index, int widthPercents) {
        final Tc tc = (Tc) row.getContent().get(index);
        final TcPr tcPr = factory.createTcPr();
        final CTShd ctShd = factory.createCTShd();
        // background: light gray
        ctShd.setFill("F2F2F2");
        tcPr.setShd(ctShd);
        tc.setTcPr(tcPr);
        setTableCellWidthPercent(tc, widthPercents);
        return tc;
    }

    private Tc tableCell(Tr row, int index) {
        final Tc tc = (Tc) row.getContent().get(index);
        final TcPr tcPr = factory.createTcPr();
        final CTShd ctShd = factory.createCTShd();
        tcPr.setShd(ctShd);
        tc.setTcPr(tcPr);
        return tc;
    }

    private P tableText(String paragraphText) {
        return tableText(paragraphText, JcEnumeration.LEFT);
    }

    private P tableText(String paragraphText, JcEnumeration justification) {
        final P p = factory.createP();

        final Text t = factory.createText();
        t.setValue(paragraphText);

        final R run = factory.createR();
        run.getContent().add(t);

        p.getContent().add(run);

        final PPr paragraphProperties = factory.createPPr();
        final Jc jc = factory.createJc();
        jc.setVal(justification);
        paragraphProperties.setJc(jc);
        p.setPPr(paragraphProperties);
        setTextSize(p);

        return p;
    }

    private void setTableCellWidthPercent(Tc tc, int percent) {
        final TcPr tcPr = tc.getTcPr();
        final TblWidth tblWidth = factory.createTblWidth();
        tblWidth.setW(BigInteger.valueOf(percent));
        tblWidth.setType("pct");
        tcPr.setTcW(tblWidth);
    }

    private void setTableWidthFull(Tbl table) {
        final TblPr tblPr = table.getTblPr();
        final TblWidth tblWidth = factory.createTblWidth();
        tblWidth.setW(BigInteger.valueOf(5000));
        tblWidth.setType("pct");
        tblPr.setTblW(tblWidth);
    }

    private void setTextSize(P p) {
        final RPr rpr = factory.createRPr();
        final HpsMeasure sz = factory.createHpsMeasure();
        sz.setVal(BigInteger.valueOf(15));
        rpr.setSz(sz);

        for (Object o : p.getContent()) {
            if (o instanceof R) {
                ((R) o).setRPr(rpr);
            }
        }
    }

    private void setTextSize(P.Hyperlink p) {
        for (Object o : p.getContent()) {
            if (o instanceof R run) {
                RPr rPr = run.getRPr();
                if (rPr == null) {
                    rPr = factory.createRPr();
                    run.setRPr(rPr);
                }
                HpsMeasure sz = rPr.getSz();
                if (sz == null) {
                    sz = factory.createHpsMeasure();
                    rPr.setSz(sz);
                }
                sz.setVal(BigInteger.valueOf(15));
            }
        }
    }

    private P numberedText(long numId, long ilvl, String paragraphText) {
        final P p = factory.createP();

        final Text t = factory.createText();
        t.setValue(paragraphText);

        final R run = factory.createR();
        run.getContent().add(t);

        p.getContent().add(run);

        final PPr ppr = factory.createPPr();
        p.setPPr(ppr);

        // Create and add <w:numPr>
        final PPrBase.NumPr numPr = factory.createPPrBaseNumPr();
        ppr.setNumPr(numPr);

        // The <w:ilvl> element
        final PPrBase.NumPr.Ilvl ilvlElement = factory.createPPrBaseNumPrIlvl();
        numPr.setIlvl(ilvlElement);
        ilvlElement.setVal(BigInteger.valueOf(ilvl));

        // The <w:numId> element
        final PPrBase.NumPr.NumId numIdElement = factory.createPPrBaseNumPrNumId();
        numPr.setNumId(numIdElement);
        numIdElement.setVal(BigInteger.valueOf(numId));

        return p;
    }

    private P newImage(WordprocessingMLPackage wordMLPackage, byte[] bytes) throws Exception {
        final BinaryPartAbstractImage imagePart = BinaryPartAbstractImage.createImagePart(wordMLPackage, bytes);
        final Inline inline = imagePart.createImageInline(null, null, imageId++, imageId++, false);

        // Now add the inline in w:p/w:r/w:drawing
        final P p = factory.createP();
        final R run = factory.createR();
        p.getContent().add(run);
        final Drawing drawing = factory.createDrawing();
        run.getContent().add(drawing);
        drawing.getAnchorOrInline().add(inline);

        final PPr paragraphProperties = factory.createPPr();
        final Jc jc = factory.createJc();
        jc.setVal(JcEnumeration.CENTER);
        paragraphProperties.setJc(jc);
        p.setPPr(paragraphProperties);

        return p;
    }

    private void attachBookmark(String name, P p) {
        final BigInteger id = BigInteger.valueOf(bookmarkId++);

        // Add bookmark end first
        final CTMarkupRange mr = factory.createCTMarkupRange();
        mr.setId(id);
        p.getContent().add(0, factory.createBodyBookmarkEnd(mr));

        // Next, bookmark start
        final CTBookmark bm = factory.createCTBookmark();
        bm.setId(id);
        bm.setName(name);
        p.getContent().add(0, factory.createBodyBookmarkStart(bm));
    }
}
