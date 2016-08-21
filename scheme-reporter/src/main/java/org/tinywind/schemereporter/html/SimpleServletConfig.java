/**
 * Copyright (c) 2016, Jeon JaeHyeong (http://github.com/tinywind)
 * All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tinywind.schemereporter.html;

import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.SimpleInstanceManager;
import org.eclipse.jetty.server.handler.ContextHandler;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Enumeration;

public class SimpleServletConfig implements ServletConfig {
    private final ServletContext servletContext = new ContextHandler.StaticContext();
    private final InstanceManager instanceManager = new SimpleInstanceManager();

    public String getServletName() {
        return null;
    }

    public ServletContext getServletContext() {
        servletContext.setAttribute(InstanceManager.class.getName(), instanceManager);
        return servletContext;
    }

    public String getInitParameter(String name) {
        return null;
    }

    public Enumeration<String> getInitParameterNames() {
        return null;
    }
}
