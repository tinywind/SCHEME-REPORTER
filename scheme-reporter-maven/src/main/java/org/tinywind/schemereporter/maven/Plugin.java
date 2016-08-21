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
package org.tinywind.schemereporter.maven;

import org.tinywind.schemereporter.SchemeReporter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import javax.script.ScriptException;
import javax.xml.bind.JAXB;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import static org.apache.maven.plugins.annotations.LifecyclePhase.GENERATE_SOURCES;
import static org.apache.maven.plugins.annotations.ResolutionScope.TEST;

/**
 * @author tinywind
 */
@Mojo(name = "generate", defaultPhase = GENERATE_SOURCES, requiresDependencyResolution = TEST)
public class Plugin extends AbstractMojo {
    /**
     * The Maven project.
     */
    @Parameter(property = "project", required = true, readonly = true)
    private MavenProject project;

    /**
     * Whether to skip the execution of the Maven Plugin for this module.
     */
    @Parameter
    private boolean skip;

    @Override
    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("Skip SCHEME-REPORTER");
            return;
        }

        getLog().info("Complete SCHEME-REPORTER");
    }
}
