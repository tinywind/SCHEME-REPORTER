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

import org.junit.Test;
import org.tinywind.schemereporter.sample.Creator;

import java.io.File;

/**
 * @author tinywind
 */
public class LauncherTest {

    @Test
    public void test() throws Exception {
        Creator.create();

        try {
            Launcher.main(new String[]{"src/test/resources/sample-configuration.xml", "src/test/resources/h2-1.4.196.jar"});
        } finally {
            new File("doc/PUBLIC.html").deleteOnExit();
            new File("doc/PUBLIC.pdf").deleteOnExit();
        }
    }
}
