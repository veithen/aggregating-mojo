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
package com.github.veithen.maven.shared.mojo.aggregating;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import com.github.veithen.rbeans.RBeanFactoryException;

public abstract class AggregatingMojo<T extends Serializable> extends AbstractMojo {
    private final Class<T> resultType;

    @Parameter(property="project", readonly=true, required=true)
    protected MavenProject project;

    @Parameter(defaultValue="${session}", readonly=true, required=true)
    protected MavenSession mavenSession;

    @Parameter(defaultValue="${mojoExecution}", readonly=true, required=true)
    protected MojoExecution mojoExecution;

    public AggregatingMojo(Class<T> resultType) {
        this.resultType = resultType;
    }

    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {
        AggregationHelper aggregationHelper;
        try {
            aggregationHelper = AggregationHelperFactory.getAggregationHelper();
        } catch (RBeanFactoryException | IOException ex) {
            throw new MojoFailureException(ex.getMessage(), ex);
        }
        Serializable result = doExecute();
        List<Serializable> results;
        try {
            results = aggregationHelper.addResult(project, mavenSession, mojoExecution, result, getClass().getClassLoader());
        } catch (ClassNotFoundException | IOException ex) {
            throw new MojoFailureException("Failed to serialize/deserialize Mojo results", ex);
        }
        if (results != null) {
            doAggregate(results.stream().map(resultType::cast).collect(Collectors.toList()));
        }
    }

    /**
     * Execute this Mojo on the current project.
     * 
     * @return the result to report to {@link #doAggregate(List)} when all executions have
     *         completed, or {@code null} if execution was skipped
     * @throws MojoExecutionException
     * @throws MojoFailureException
     */
    protected abstract T doExecute() throws MojoExecutionException, MojoFailureException;

    protected abstract void doAggregate(List<T> results) throws MojoExecutionException, MojoFailureException;
}
