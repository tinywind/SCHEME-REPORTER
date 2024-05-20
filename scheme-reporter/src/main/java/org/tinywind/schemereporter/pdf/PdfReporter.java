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
package org.tinywind.schemereporter.pdf;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.action.PdfAction;
import com.itextpdf.kernel.pdf.canvas.draw.DottedLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TabAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.commons.io.IOUtils;
import org.jooq.meta.*;
import org.jooq.tools.JooqLogger;
import org.jooq.tools.StringUtils;
import org.tinywind.schemereporter.Reportable;
import org.tinywind.schemereporter.jaxb.Generator;
import org.tinywind.schemereporter.util.TableImage;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.tinywind.schemereporter.util.TableImage.pngBytesFromSvg;

public class PdfReporter implements Reportable {
    private static final JooqLogger log = JooqLogger.getLogger(PdfReporter.class);
    private Database database;
    private Generator generator;
    private final Color lightGrey = new DeviceRgb(0xF5, 0xF5, 0xF5);
    private final Color darkBlue = new DeviceRgb(0x00, 0x00, 0x60);

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

        final byte[] fontBytes = IOUtils.toByteArray(Objects.requireNonNull(getClass().getResourceAsStream("/asset/font/NanumMyeongjo.ttf")));
        final PdfFont font = PdfFontFactory.createFont(fontBytes, PdfEncodings.IDENTITY_H);

        final SchemaVersionProvider schemaVersionProvider = schema.getDatabase().getSchemaVersionProvider();
        final String version = schemaVersionProvider != null ? schemaVersionProvider.version(schema) : null;
        StringBuilder revise = new StringBuilder();
        File file;
        while ((file = new File(generator.getOutputDirectory(), schema.getName() + (!StringUtils.isEmpty(version) ? "-" + version : "") + revise + ".pdf")).exists()) {
            revise.append("_");
        }

        log.info("output file: " + file);
        final File path = file.getParentFile();
        if (path != null) {
            if (path.mkdirs()) log.info("created path: " + path);
            else log.error("failed to create path: " + path);
        }

        final List<EnumDefinition> enums = database.getEnums(schema);
        final List<TableDefinition> tables = database.getTables(schema);
        final String totalRelationSvg = TableImage.totalRelationSvg(tables);
        final Map<String, String> relationSvg = TableImage.relationSvg(tables);

        final PageSize PAGE_SIZE = PageSize.A4;
        final float PAGE_MARGIN = 30;
        final float MAX_WIDTH = PAGE_SIZE.getWidth() - PAGE_MARGIN * 2;
        final float MAX_HEIGHT = PAGE_SIZE.getWidth() - PAGE_MARGIN * 2;
        final float svgScaleRate = 0.6f;

        final PdfDocument pdf = new PdfDocument(new PdfWriter(new FileOutputStream(file)));
        final Document document = new Document(pdf, PAGE_SIZE);
        document.setMargins(PAGE_MARGIN, PAGE_MARGIN, PAGE_MARGIN, PAGE_MARGIN);
        document.setFont(font);

        document.add(new Paragraph(new Text("Table of contents").setFontSize(14).setBold()).setTextAlignment(TextAlignment.CENTER));

        if (!enums.isEmpty()) {
            document.add(itemOfTableContents("Enum Definition", "Enum Definition"));
            for (Definition e : enums)
                document.add(leafItemOfTableContents(e.getName(), "enum$" + e.getName()));
        }

        document.add(itemOfTableContents("Table Definition", "Table Definition"));
        for (Definition e : tables)
            document.add(leafItemOfTableContents(e.getName(), "table$" + e.getName()));
        document.add(new AreaBreak());

        if (!enums.isEmpty()) {
            final Div divEnums = chapter("Enum Definition");
            for (EnumDefinition e : enums)
                divEnums.add(enumElement(e));
            document.add(divEnums);
            document.add(new AreaBreak());
        }

        final Div divTableOverview = chapter("Table Definition");
        divTableOverview.add(new Paragraph().add(createImage(totalRelationSvg, svgScaleRate, MAX_WIDTH, MAX_HEIGHT)).setTextAlignment(TextAlignment.CENTER));
        document.add(divTableOverview);
        document.add(new AreaBreak());

        for (int i = 0; i < tables.size(); i++) {
            final TableDefinition e = tables.get(i);
            final Div div = tableTitle(e).add(new Paragraph().add(createImage(relationSvg.get(e.getName()), svgScaleRate, MAX_WIDTH, MAX_HEIGHT)).setTextAlignment(TextAlignment.CENTER)).add(tableDefinition(e));

            if (!e.getUniqueKeys().isEmpty()) div.add(tableUniqueKey(e));

            if (e.getColumns().stream().anyMatch(c -> !StringUtils.isEmpty(c.getComment())))
                div.add(tableColumnComments(e));

            document.add(div);

            if (i + 1 < tables.size()) document.add(new AreaBreak());
        }

        document.close();
    }

    private Image createImage(String svg, float svgScaleRate, float maxWidth, float maxHeight) throws TranscoderException {
        final Image img = new Image(ImageDataFactory.createPng(pngBytesFromSvg(svg)));
        final float width = img.getImageScaledWidth();
        final float height = img.getImageScaledHeight();
        final float scaleRate;
        if (width / maxWidth > height / maxHeight) {
            if (width < maxWidth) {
                scaleRate = svgScaleRate;
            } else {
                final float rate = (maxWidth / width);
                scaleRate = Math.min(rate, svgScaleRate);
            }
        } else {
            if (height < maxHeight) {
                scaleRate = svgScaleRate;
            } else {
                final float rate = (maxHeight / height);
                scaleRate = Math.min(rate, svgScaleRate);
            }
        }
        img.scale(scaleRate, scaleRate);
        return img;
    }

    private Div chapter(String title) {
        final Div div = new Div().setDestination(title);

        div.add(new Paragraph(title).setFontSize(20).setPaddingBottom(10));

        return div;
    }

    private Paragraph itemOfTableContents(String name, String desc) {
        return new Paragraph().setAction(PdfAction.createGoTo(name)).addTabStops(new TabStop(540, TabAlignment.RIGHT, new DottedLine())).add(new Text(desc).setFontSize(12).setItalic()).add(new Tab());
    }

    private Paragraph leafItemOfTableContents(String name, String desc) {
        return new Paragraph().setAction(PdfAction.createGoTo(desc)).addTabStops(new TabStop(540, TabAlignment.RIGHT, new DottedLine())).add(new Text(".     " + name).setFontSize(9)).add(new Tab());
    }

    private <T extends IElement> Cell headerCell(BlockElement<T> element) {
        return new Cell().add(element).setBorder(Border.NO_BORDER).setBackgroundColor(lightGrey).setFontSize(8);
    }

    private Cell headerCell(String name) {
        return headerCell(new Paragraph(name));
    }

    private <T extends IElement> Cell cell(BlockElement<T> element, boolean underline) {
        final Cell cell = new Cell().add(element).setBorder(new SolidBorder(ColorConstants.WHITE, 1)).setFontSize(8);
        if (underline) cell.setBorderBottom(new SolidBorder(lightGrey, 1));
        return cell;
    }

    private <T extends IElement> Cell cell(BlockElement<T> element) {
        return cell(element, false);
    }

    private Cell cell(String name, boolean underline) {
        return cell(new Paragraph(name), underline);
    }

    private Cell cell(String name) {
        return cell(name, false);
    }

    private Div tableTitle(TableDefinition e) {
        final Paragraph title = new Paragraph().add(new Text(e.getName()).setFontSize(12));
        if (e.getComment() != null) {
            final Text textComment = new Text("  " + e.getComment()).setFontSize(8).setFontColor(ColorConstants.DARK_GRAY).setTextRise(4);
            title.add(textComment);
        }

        return new Div().setDestination("table$" + e.getName()).add(title);
    }

    private BlockElement<?> tableDefinition(TableDefinition e) {
        final Table table = new Table(new float[]{20, 20, 6, 6, 8, 20, 20}).setWidth(UnitValue.createPercentValue(100)).addHeaderCell(headerCell("column")).addHeaderCell(headerCell("type")).addHeaderCell(headerCell("nullable").setTextAlignment(TextAlignment.CENTER)).addHeaderCell(headerCell("pkey").setTextAlignment(TextAlignment.CENTER)).addHeaderCell(headerCell("defaulted").setTextAlignment(TextAlignment.CENTER)).addHeaderCell(headerCell("referred")).addHeaderCell(headerCell("refer"));

        for (int i = 0; i < e.getColumns().size(); i++) {
            final boolean presentBottom = i + 1 < e.getColumns().size();
            final ColumnDefinition column = e.getColumn(i);
            table.addCell(cell(column.getName(), presentBottom));
            final String type = column.getType().getType();
            if (type.equalsIgnoreCase("USER-DEFINED")) {
                final String userType = column.getType().getUserType();
                table.addCell(cell(userType, presentBottom).setAction(PdfAction.createGoTo("enum$" + userType)).setItalic().setFontColor(darkBlue));
            } else {
                table.addCell(cell(type + (column.getType().getLength() != 0 ? "(" + column.getType().getLength() + ")" : ""), presentBottom));
            }

            table.addCell(cell(column.getType().isNullable() ? "●" : "", presentBottom).setTextAlignment(TextAlignment.CENTER));
            table.addCell(cell(column.getPrimaryKey() != null ? "●" : "", presentBottom).setTextAlignment(TextAlignment.CENTER));
            table.addCell(cell(column.getType().isDefaulted() ? "●" : "", presentBottom).setTextAlignment(TextAlignment.CENTER));

            final Div divUniqueKey = new Div();
            for (UniqueKeyDefinition key : column.getUniqueKeys())
                putKeyDescriptions(divUniqueKey, key.getForeignKeys(), false);
            table.addCell(cell(divUniqueKey, presentBottom));

            final Div divForeignKey = new Div();
            putKeyDescriptions(divForeignKey, column.getForeignKeys(), true);
            table.addCell(cell(divForeignKey, presentBottom));
        }

        return table;
    }

    private BlockElement<?> tableUniqueKey(TableDefinition e) {
        final Table table = new Table(new float[]{20, 30, 50}).setWidth(UnitValue.createPercentValue(100)).addHeaderCell(headerCell("unique key")).addHeaderCell(headerCell("columns")).addHeaderCell(headerCell("description"));

        for (int i = 0; i < e.getUniqueKeys().size(); i++) {
            final boolean presentBottom = i + 1 < e.getUniqueKeys().size();
            final UniqueKeyDefinition ukey = e.getUniqueKeys().get(i);
            table.addCell(cell(ukey.getName(), presentBottom).setDestination("key$" + ukey.getName()));
            StringBuilder text = new StringBuilder();
            for (ColumnDefinition column : ukey.getKeyColumns()) text.append(column.getName()).append(" ");
            table.addCell(cell(text.toString(), presentBottom));
            table.addCell(cell(ukey.getComment() != null ? ukey.getComment() : "", presentBottom));
        }

        return table;
    }

    private BlockElement<?> tableColumnComments(TableDefinition e) {
        final Table table = new Table(new float[]{20, 80}).setWidth(UnitValue.createPercentValue(100)).addHeaderCell(headerCell("column")).addHeaderCell(headerCell("description"));

        final java.util.List<ColumnDefinition> list = e.getColumns().stream().filter(column -> column.getComment() != null).toList();
        for (int i = 0; i < list.size(); i++) {
            final boolean presentBottom = i + 1 < list.size();
            final ColumnDefinition column = list.get(i);
            if (StringUtils.isEmpty(column.getComment())) continue;
            table.addCell(cell(column.getName(), presentBottom));
            table.addCell(cell(column.getComment(), presentBottom));
        }
        return table;
    }

    private void putKeyDescriptions(Div div, java.util.List<ForeignKeyDefinition> foreignKeys, boolean referenced) {
        for (ForeignKeyDefinition fkey : foreignKeys) {
            StringBuilder text = new StringBuilder("[" + (referenced ? fkey.getReferencedTable().getName() : fkey.getKeyTable().getName()) + "]");
            if (referenced)
                for (ColumnDefinition column : fkey.getReferencedColumns()) text.append(" ").append(column.getName());
            else for (ColumnDefinition column : fkey.getKeyColumns()) text.append(" ").append(column.getName());
            final Paragraph paragraph = new Paragraph(text.toString());
            div.add(referenced ? paragraph.setItalic().setFontColor(darkBlue).setAction(PdfAction.createGoTo("key$" + fkey.getReferencedKey().getName())) : paragraph);
        }
    }

    private BlockElement<?> enumElement(EnumDefinition e) {
        final String name = e.getName();
        final java.util.List<String> literals = e.getLiterals();

        final Paragraph title = new Paragraph().add(new Text(name).setFontSize(12));
        if (e.getComment() != null) {
            final Text textComment = new Text("  " + e.getComment()).setFontSize(8).setFontColor(ColorConstants.DARK_GRAY).setTextRise(4);
            title.add(textComment);
        }

        StringBuilder string = new StringBuilder();
        for (int i = 0; i < literals.size(); i++)
            string.append(literals.get(i)).append(i + 1 < literals.size() ? ", " : "");
        final Paragraph elements = new Paragraph(string.toString()).setFontSize(10);

        final Table table = new Table(1).setWidth(UnitValue.createPercentValue(100));
        table.addCell(headerCell(title).setPadding(5));
        table.addCell(cell(elements).setPadding(3).setPaddingLeft(5));
        table.setDestination("enum$" + name);
        table.setMarginBottom(10);
        table.setWidth(UnitValue.createPercentValue(100));
        return table;
    }
}
