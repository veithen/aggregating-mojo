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

import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.plugin.MojoExecution;

final class AggregationKey {
    private final String pluginId;
    private final String goal;

    private AggregationKey(String pluginId, String goal) {
        this.pluginId = pluginId;
        this.goal = goal;
    }

    static AggregationKey from(MojoExecution mojoExecution) {
        return new AggregationKey(mojoExecution.getPlugin().getId(), mojoExecution.getGoal());
    }

    static Set<AggregationKey> from(Plugin plugin, PluginExecution pluginExecution) {
        String pluginId = plugin.getId();
        return pluginExecution.getGoals().stream()
                .map(goal -> new AggregationKey(pluginId, goal))
                .collect(Collectors.toSet());
    }

    @Override
    public int hashCode() {
        return pluginId.hashCode() + 31 * goal.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AggregationKey) {
            AggregationKey other = (AggregationKey) obj;
            return other.pluginId.equals(pluginId) && other.goal.equals(goal);
        } else {
            return false;
        }
    }
}
