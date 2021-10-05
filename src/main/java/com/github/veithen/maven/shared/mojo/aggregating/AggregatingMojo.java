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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

public abstract class AggregatingMojo<T extends Serializable> extends AbstractMojo {
    private final Class<T> resultType;

    @Parameter(property = "project", readonly = true, required = true)
    protected MavenProject project;

    @Parameter(property = "session", readonly = true, required = true)
    protected MavenSession mavenSession;

    @Parameter(property = "mojoExecution", readonly = true, required = true)
    protected MojoExecution mojoExecution;

    public AggregatingMojo(Class<T> resultType) {
        this.resultType = resultType;
    }

    private String getContextKey(String executionId) {
        return AggregatingMojo.class.getName()
                + ":"
                + mojoExecution.getPlugin().getId()
                + ":"
                + mojoExecution.getGoal()
                + ":"
                + executionId;
    }

    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {
        MavenProject topLevelProject = mavenSession.getTopLevelProject();
        Object lock;
        synchronized (topLevelProject) {
            String key = getContextKey("__lock__");
            lock = topLevelProject.getContextValue(key);
            if (lock == null) {
                lock = new Object();
                topLevelProject.setContextValue(key, lock);
            }
        }

        byte[] currentSerializedResult;
        try {
            Serializable currentResult = doExecute();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(currentResult);
            oos.close();
            currentSerializedResult = baos.toByteArray();
        } catch (IOException ex) {
            throw new MojoFailureException("Failed to serialize Mojo results", ex);
        }

        boolean isLast = true;
        List<byte[]> serializedResults = new ArrayList<>();
        synchronized (lock) {
            synchronized (project) {
                project.setContextValue(
                        getContextKey(mojoExecution.getExecutionId()), currentSerializedResult);
            }
            outer:
            for (MavenProject project : mavenSession.getProjects()) {
                for (Plugin plugin : project.getBuildPlugins()) {
                    if (plugin.getId().equals(mojoExecution.getPlugin().getId())) {
                        for (PluginExecution execution : plugin.getExecutions()) {
                            if (execution.getGoals().contains(mojoExecution.getGoal())) {
                                byte[] result;
                                synchronized (project) {
                                    result =
                                            (byte[])
                                                    project.getContextValue(
                                                            getContextKey(execution.getId()));
                                }
                                if (result == null) {
                                    isLast = false;
                                    break outer;
                                }
                                serializedResults.add(result);
                            }
                        }
                    }
                }
            }
        }

        if (isLast) {
            List<Serializable> results = new ArrayList<>(serializedResults.size());
            for (byte[] serializedResult : serializedResults) {
                try {
                    results.add(
                            (Serializable)
                                    new ConfigurableObjectInputStream(
                                                    new ByteArrayInputStream(serializedResult),
                                                    getClass().getClassLoader())
                                            .readObject());
                } catch (ClassNotFoundException | IOException ex) {
                    throw new MojoFailureException("Failed to deserialize Mojo results", ex);
                }
            }
            doAggregate(results.stream().map(resultType::cast).collect(Collectors.toList()));
        }
    }

    /**
     * Execute this Mojo on the current project.
     *
     * @return the result to report to {@link #doAggregate(List)} when all executions have
     *     completed, or {@code null} if execution was skipped
     * @throws MojoExecutionException
     * @throws MojoFailureException
     */
    protected abstract T doExecute() throws MojoExecutionException, MojoFailureException;

    protected abstract void doAggregate(List<T> results)
            throws MojoExecutionException, MojoFailureException;
}
