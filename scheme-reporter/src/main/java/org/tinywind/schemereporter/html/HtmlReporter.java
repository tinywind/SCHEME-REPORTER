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
package org.tinywind.schemereporter.html;

import org.jooq.meta.Database;
import org.jooq.meta.SchemaDefinition;
import org.jooq.meta.SchemaVersionProvider;
import org.jooq.meta.TableDefinition;
import org.jooq.tools.JooqLogger;
import org.jooq.tools.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.FileTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.tinywind.schemereporter.Reportable;
import org.tinywind.schemereporter.jaxb.Generator;
import org.tinywind.schemereporter.util.FileUtils;

import java.io.File;
import java.util.List;

import static org.tinywind.schemereporter.util.TableImage.relationSvg;
import static org.tinywind.schemereporter.util.TableImage.totalRelationSvg;

public class HtmlReporter implements Reportable {
    private static final JooqLogger log = JooqLogger.getLogger(HtmlReporter.class);

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
    public void generate(SchemaDefinition schema) throws Exception {
        final SchemaVersionProvider schemaVersionProvider = schema.getDatabase().getSchemaVersionProvider();
        final String version = schemaVersionProvider != null ? schemaVersionProvider.version(schema) : null;

        final TemplateData templateData = getTemplateData(generator.getTemplate());
        final TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateData.templateResolver);

        final Context context = new Context();
        final List<TableDefinition> tables = database.getTables(schema);
        context.setVariable("utils", new TemplateUtils());
        context.setVariable("totalRelationSvg", totalRelationSvg(tables));
        context.setVariable("relationSvg", relationSvg(tables));
        context.setVariable("database", database);
        context.setVariable("enums", database.getEnums(schema));
        context.setVariable("sequences", database.getSequences(schema));
        context.setVariable("tables", tables);

        final String content = templateEngine.process(templateData.templateName, context);
        final File file = FileUtils.getOutputFile(generator.getOutputDirectory(), "html", schema.getName(), version);
        log.info("output file: " + file);
        org.apache.commons.io.FileUtils.write(file, content, "UTF-8");
    }

    private TemplateData getTemplateData(String templateFilePath) {
        if (StringUtils.isEmpty(templateFilePath)) {
            final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
            templateResolver.setPrefix("/asset/");
            templateResolver.setSuffix(".html");
            templateResolver.setTemplateMode("HTML5");
            templateResolver.setCharacterEncoding("UTF-8");
            return new TemplateData(templateResolver, "default");
        }

        final File templateFile = new File(templateFilePath);
        final FileTemplateResolver templateResolver = new FileTemplateResolver();
        templateResolver.setPrefix(templateFile.getParent() + "/");
        templateResolver.setSuffix(templateFile.getName().lastIndexOf(".") == -1 ? "" : templateFile.getName().substring(templateFile.getName().lastIndexOf(".")));
        templateResolver.setTemplateMode("HTML5");
        templateResolver.setCharacterEncoding("UTF-8");
        return new TemplateData(templateResolver, templateFile.getName().lastIndexOf(".") == -1 ? templateFile.getName() : templateFile.getName().substring(0, templateFile.getName().lastIndexOf(".")));
    }

    static class TemplateData {
        ITemplateResolver templateResolver;
        String templateName;

        TemplateData(ITemplateResolver templateResolver, String templateName) {
            this.templateResolver = templateResolver;
            this.templateName = templateName;
        }
    }
}
