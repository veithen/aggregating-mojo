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

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;

public class AggregationHelperImpl {
    private static final Map<MavenSession, Map<AggregationKey, Aggregator>> sessionMap = new WeakHashMap<>();

    public static List<Serializable> addResult(MavenProject project, MavenSession mavenSession, MojoExecution mojoExecution, Serializable result, ClassLoader classLoader) throws ClassNotFoundException, IOException {
        AggregationKey aggregationKey = AggregationKey.from(mojoExecution);
        ExecutionKey executionKey = ExecutionKey.from(project, mojoExecution);
        Map<AggregationKey, Aggregator> aggregators;
        synchronized (sessionMap) {
            aggregators = sessionMap.computeIfAbsent(mavenSession, k -> new HashMap<>());
        }
        Aggregator aggregator;
        synchronized (aggregators) {
            aggregator = aggregators.computeIfAbsent(aggregationKey, k -> new Aggregator(mavenSession, k));
        }
        return aggregator.addResult(executionKey, result, classLoader);
    }
}
