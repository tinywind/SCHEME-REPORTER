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
package org.tinywind.schemereporter.sample;

import org.h2.Driver;
import org.h2.tools.Server;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * @author tinywind
 */
public class Creator {
    public static Connection connection() throws SQLException {

        final String url = "jdbc:h2:tcp://localhost:9092/mem:test;DB_CLOSE_DELAY=-1";
        final Properties properties = new Properties();
        properties.put("user", "sa");
        properties.put("password", "");

        return new Driver().connect(url, properties);
    }

    public static void create() throws SQLException {
        Server.createTcpServer().start();

        try (final Connection connect = connection()) {
            try (final Statement statement = connect.createStatement()) {
                statement.execute("CREATE TABLE parent (" +
                        "id int AUTO_INCREMENT NOT NULL COMMENT 'identifier'," +
                        "name varchar(32) NOT NULL COMMENT 'name of parent'," +
                        "age int," +
                        "CONSTRAINT parent_pkey PRIMARY KEY (id)" +
                        ")");
            }

            try (final Statement statement = connect.createStatement()) {
                statement.execute("CREATE TABLE child (" +
                        "id int AUTO_INCREMENT NOT NULL COMMENT 'identifier'," +
                        "name varchar(32) NOT NULL COMMENT 'name of child'," +
                        "age int," +
                        "parent_id int NOT NULL COMMENT 'parent id'," +
                        "CONSTRAINT child_pkey PRIMARY KEY (id)" +
                        ")");
            }

            try (final Statement statement = connect.createStatement()) {
                statement.execute("ALTER TABLE child ADD FOREIGN KEY (parent_id) REFERENCES parent(id)");
            }
        }
    }
}
