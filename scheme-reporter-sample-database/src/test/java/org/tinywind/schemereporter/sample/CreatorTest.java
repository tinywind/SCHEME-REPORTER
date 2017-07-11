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

import org.junit.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author tinywind
 */
public class CreatorTest {
    @Test
    public void test() throws SQLException, IOException {
        Creator.create();

        try (final Connection connection = Creator.connection()) {
            try (final Statement statement = connection.createStatement()) {
                statement.execute("INSERT INTO public_file (original_name, name, size) VALUES ('original_name', 'name', 0)");
            }

            try (final Statement statement = connection.createStatement()) {
                final ResultSet resultSet = statement.executeQuery("SELECT * FROM public_file");

                int size = 0;
                while (resultSet.next()) size++;

                assert size == 1;
            }
        }
    }
}
