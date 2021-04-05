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
import java.io.InputStream;

import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.util.IOUtil;

import com.github.veithen.rbeans.RBeanFactory;
import com.github.veithen.rbeans.RBeanFactoryException;

final class AggregationHelperFactory {
    private static final String[] classesToInject = {
        AggregationHelper.IMPL_CLASS,
        "com.github.veithen.maven.shared.mojo.aggregating.helper.AggregationKey",
        "com.github.veithen.maven.shared.mojo.aggregating.helper.Aggregator",
        "com.github.veithen.maven.shared.mojo.aggregating.helper.ConfigurableObjectInputStream",
        "com.github.veithen.maven.shared.mojo.aggregating.helper.ExecutionKey",
    };

    private static AggregationHelper aggregationHelper;

    static synchronized AggregationHelper getAggregationHelper()
            throws RBeanFactoryException, IOException {
        if (aggregationHelper == null) {
            ClassLoader targetClassLoader = MavenSession.class.getClassLoader();
            synchronized (targetClassLoader) {
                try {
                    targetClassLoader.loadClass(AggregationHelper.IMPL_CLASS);
                } catch (ClassNotFoundException ex) {
                    ClassLoaderRBean targetClassLoaderRBean =
                            new RBeanFactory(ClassLoaderRBean.class)
                                    .createRBean(ClassLoaderRBean.class, targetClassLoader);
                    ClassLoader sourceClassLoader = AggregationHelperFactory.class.getClassLoader();
                    for (String className : classesToInject) {
                        try (InputStream in =
                                sourceClassLoader.getResourceAsStream(
                                        className.replace('.', '/') + ".class")) {
                            byte[] data = IOUtil.toByteArray(in);
                            targetClassLoaderRBean.defineClass(className, data, 0, data.length);
                        }
                    }
                }
            }
            aggregationHelper =
                    new RBeanFactory(targetClassLoader, AggregationHelper.class)
                            .createRBean(AggregationHelper.class);
        }
        return aggregationHelper;
    }
}
