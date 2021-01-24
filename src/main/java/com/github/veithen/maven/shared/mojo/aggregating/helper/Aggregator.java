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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;

final class Aggregator {
    private final Set<ExecutionKey> executionKeys = new HashSet<>();
    private final List<byte[]> serializedResults;

    Aggregator(MavenSession mavenSession, AggregationKey aggregationKey) {
        for (MavenProject project : mavenSession.getProjects()) {
            for (Plugin plugin : project.getBuildPlugins()) {
                for (PluginExecution execution : plugin.getExecutions()) {
                    if (AggregationKey.from(plugin, execution).contains(aggregationKey)) {
                        executionKeys.add(ExecutionKey.from(project, execution));
                    }
                }
            }
        }
        serializedResults = new ArrayList<>(executionKeys.size());
    }

    @SuppressWarnings("BanSerializableRead")
    List<Serializable> addResult(
            ExecutionKey executionKey, Serializable result, ClassLoader classLoader)
            throws IOException, ClassNotFoundException {
        if (result != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(result);
            oos.close();
            serializedResults.add(baos.toByteArray());
        }
        boolean isLast;
        synchronized (executionKeys) {
            executionKeys.remove(executionKey);
            isLast = executionKeys.isEmpty();
        }
        if (isLast) {
            List<Serializable> results = new ArrayList<>(serializedResults.size());
            for (byte[] serializedResult : serializedResults) {
                results.add(
                        (Serializable)
                                new ConfigurableObjectInputStream(
                                                new ByteArrayInputStream(serializedResult),
                                                classLoader)
                                        .readObject());
            }
            return results;
        } else {
            return null;
        }
    }
}
