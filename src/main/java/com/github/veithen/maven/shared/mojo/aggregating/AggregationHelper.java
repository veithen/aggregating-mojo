/*-
 * #%L
 * aggregating-mojo
 * %%
 * Copyright (C) 2018 - 2021 Andreas Veithen
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.github.veithen.maven.shared.mojo.aggregating;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;

import com.github.veithen.rbeans.StaticRBean;
import com.github.veithen.rbeans.Target;

@Target(AggregationHelper.IMPL_CLASS)
interface AggregationHelper extends StaticRBean {
    String IMPL_CLASS =
            "com.github.veithen.maven.shared.mojo.aggregating.helper.AggregationHelperImpl";

    List<Serializable> addResult(
            MavenProject project,
            MavenSession mavenSession,
            MojoExecution mojoExecution,
            Serializable result,
            ClassLoader classLoader)
            throws ClassNotFoundException, IOException;
}
