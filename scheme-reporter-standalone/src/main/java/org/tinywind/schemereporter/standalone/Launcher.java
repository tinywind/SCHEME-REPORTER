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
package org.tinywind.schemereporter.standalone;

import org.jooq.tools.JooqLogger;
import org.tinywind.schemereporter.SchemeReporter;
import org.tinywind.schemereporter.jaxb.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;

import static java.lang.System.exit;

/**
 * @author tinywind
 */
public class Launcher {
    private static final JooqLogger log = JooqLogger.getLogger(Launcher.class);

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            log.error("Usage : <configuration-file> [<jdbc-class-jar>]");
            exit(-1);
        }

        final File configFile = new File(args[0]);

        if (!configFile.exists() || configFile.isDirectory()) {
            log.error(args[0] + " is invalid.");
            log.error("Usage : <configuration-file> [<jdbc-class-jar>]");
            exit(-1);
        }

        final Configuration configuration = SchemeReporter.load(new FileInputStream(configFile));

        if (args.length > 1) {
            final File jdbcJar = new File(args[1]);
            final URLClassLoader loader = new URLClassLoader(new URL[]{jdbcJar.toURI().toURL()});
            @SuppressWarnings("unchecked") final Class<? extends Driver> jdbcClass = (Class<? extends Driver>) loader.loadClass(configuration.getJdbc().getDriverClass());
            SchemeReporter.generate(configuration, config -> jdbcClass);
        } else {
            SchemeReporter.generate(configuration);
        }
    }
}
