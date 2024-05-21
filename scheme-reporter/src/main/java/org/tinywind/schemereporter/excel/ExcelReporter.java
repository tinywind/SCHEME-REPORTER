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
package org.tinywind.schemereporter.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jooq.meta.*;
import org.jooq.tools.JooqLogger;
import org.tinywind.schemereporter.Reportable;
import org.tinywind.schemereporter.jaxb.Generator;
import org.tinywind.schemereporter.util.FileUtils;
import org.tinywind.schemereporter.util.TableImage;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

public class ExcelReporter implements Reportable {
    private static final JooqLogger log = JooqLogger.getLogger(ExcelReporter.class);
    private static final int MAX_COLUMN_WIDTH = 12000;
    private static int iColumn = 0;
    private static final int INDEX_COLUMN = iColumn++;
    private static final int INDEX_TYPE = iColumn++;
    private static final int INDEX_NULLABLE = iColumn++;
    private static final int INDEX_PKEY = iColumn++;
    private static final int INDEX_DEFAULTED = iColumn++;
    private static final int INDEX_UKEY = iColumn++;
    private static final int INDEX_REFERRED = iColumn++;
    private static final int INDEX_REFER = iColumn++;
    private static final int INDEX_DESCRIPTION = iColumn;
    private static final int MAX_INDEX_COLUMN = iColumn;
    private Database database;
    private Generator generator;

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
        final File file = FileUtils.getOutputFile(generator.getOutputDirectory(), "xlsx", schema.getName(), version);

        final List<EnumDefinition> enums = database.getEnums(schema);
        final List<TableDefinition> tables = database.getTables(schema);
        final String totalRelationSvg = TableImage.totalRelationSvg(tables);
        final Map<String, String> relationSvg = TableImage.relationSvg(tables);

        try (final Workbook workbook = new XSSFWorkbook()) {

            final CellStyle style = workbook.createCellStyle();
            final CellStyle leadStyle = workbook.createCellStyle();
            final CellStyle headerStyle = workbook.createCellStyle();

            style.setBorderTop(BorderStyle.THIN);
            style.setBorderRight(BorderStyle.THIN);
            style.setBorderBottom(BorderStyle.THIN);
            style.setBorderLeft(BorderStyle.THIN);
            style.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
            style.setRightBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
            style.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
            style.setLeftBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
            style.setWrapText(true);

            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
            headerStyle.setRightBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
            headerStyle.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
            headerStyle.setLeftBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
            headerStyle.setWrapText(true);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            final Font font = workbook.createFont();
            font.setFontHeightInPoints((short) 12);
            font.setBold(true);
            leadStyle.setFont(font);

            final Sheet sheet = workbook.createSheet("tables");

            for (int iTable = 0, iRow = 0; iTable < tables.size(); iTable++, iRow++) {
                final TableDefinition table = tables.get(iTable);

                final Cell tableCell = sheet.createRow(iRow++).createCell(0);
                tableCell.setCellValue(table.getName());
                tableCell.setCellStyle(leadStyle);
                createHeader(sheet, iRow++, headerStyle);

                for (int iColumn = 0; iColumn < table.getColumns().size(); iColumn++) {
                    final ColumnDefinition column = table.getColumns().get(iColumn);
                    final Row row = sheet.createRow(iRow++);

                    create(row, INDEX_COLUMN, column.getName(), style);
                    create(row, INDEX_TYPE, column.getType().getType().equals("USER-DEFINED") ? column.getType().getUserType() : column.getType().getType() + (column.getType().getLength() > 0 ? " (" + column.getType().getLength() + ")" : ""), style);
                    create(row, INDEX_NULLABLE, column.getType().isNullable() ? "O" : "", style);
                    create(row, INDEX_PKEY, column.getPrimaryKey() != null ? "O" : "", style);

                    final StringBuilder ukeyString = new StringBuilder();
                    table.getUniqueKeys().stream().filter(e -> e.getKeyColumns().contains(column)).forEach(e -> ukeyString.append(e.getName()).append("\n"));
                    create(row, INDEX_UKEY, ukeyString.toString().trim(), style);
                    create(row, INDEX_DEFAULTED, column.getType().isDefaulted() ? "O" : "", style);

                    final StringBuilder referredString = new StringBuilder();
                    column.getUniqueKeys().forEach(ukey -> ukey.getForeignKeys().forEach(fkey -> {
                        referredString.append("[").append(fkey.getKeyTable().getName()).append("] ");
                        fkey.getKeyColumns().forEach(keyColumn -> referredString.append(keyColumn.getName()).append(" "));
                        referredString.append("\n");
                    }));
                    create(row, INDEX_REFERRED, referredString.toString().trim(), style);

                    final StringBuilder referString = new StringBuilder();
                    column.getForeignKeys().forEach(fkey -> {
                        referString.append("[").append(fkey.getReferencedTable().getName()).append("] ");
                        fkey.getReferencedColumns().forEach(keyColumn -> referString.append(keyColumn.getName()).append(" "));
                        referString.append("\n");
                    });
                    create(row, INDEX_REFER, referString.toString().trim(), style);
                    create(row, INDEX_DESCRIPTION, column.getComment(), style);
                }
            }

            for (int i = 0; i <= MAX_INDEX_COLUMN; i++) {
                sheet.autoSizeColumn(i);
                if (MAX_COLUMN_WIDTH < sheet.getColumnWidth(i)) sheet.setColumnWidth(i, MAX_COLUMN_WIDTH);
            }

            workbook.write(new FileOutputStream(file));
        }
    }

    private void createHeader(Sheet sheet, int iRow, CellStyle style) {
        final Row row = sheet.createRow(iRow);

        create(row, INDEX_COLUMN, "column", style);
        create(row, INDEX_TYPE, "type", style);
        create(row, INDEX_NULLABLE, "nullable", style);
        create(row, INDEX_PKEY, "pkey", style);
        create(row, INDEX_UKEY, "ukey", style);
        create(row, INDEX_DEFAULTED, "defaulted", style);
        create(row, INDEX_REFERRED, "referred", style);
        create(row, INDEX_REFER, "refer", style);
        create(row, INDEX_DESCRIPTION, "description", style);
    }

    private void create(Row row, int iColumn, String string, CellStyle style) {
        Cell cell = row.createCell(iColumn);
        cell.setCellStyle(style);
        cell.setCellValue(string);
    }
}
