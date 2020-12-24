/*-
 * #%L
 * aggregating-mojo
 * %%
 * Copyright (C) 2018 Andreas Veithen
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
package com.github.veithen.maven.shared.mojo.aggregating.helper;

import org.apache.maven.model.PluginExecution;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;

final class ExecutionKey {
    private final String projectId;
    private final String executionId;

    private ExecutionKey(String projectId, String executionId) {
        this.projectId = projectId;
        this.executionId = executionId;
    }

    static ExecutionKey from(MavenProject project, MojoExecution mojoExecution) {
        return new ExecutionKey(project.getId(), mojoExecution.getExecutionId());
    }

    static ExecutionKey from(MavenProject project, PluginExecution pluginExecution) {
        return new ExecutionKey(project.getId(), pluginExecution.getId());
    }

    @Override
    public int hashCode() {
        return projectId.hashCode() + 31 * executionId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ExecutionKey) {
            ExecutionKey other = (ExecutionKey) obj;
            return other.projectId.equals(projectId) && other.executionId.equals(executionId);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return projectId + ":" + executionId;
    }
}
