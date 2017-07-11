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
import org.h2.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
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

	public static void create() throws SQLException, IOException {
		Server.createTcpServer().start();

		try (final Connection connect = connection()) {

			final byte[] file = new byte[1024 * 1024];
			InputStream input = Creator.class.getResourceAsStream("/ddl.sql");
			int read = input.read(file);
			String content = new String(file, 0, read, "UTF-8");
			String[] strings = content.split(";");

			for (String string : strings) {
				final String query = string.trim();
				if (StringUtils.isNullOrEmpty(query))
					continue;

				try (final Statement statement = connect.createStatement()) {
					statement.execute(query);
				}
			}
		}
	}
}
