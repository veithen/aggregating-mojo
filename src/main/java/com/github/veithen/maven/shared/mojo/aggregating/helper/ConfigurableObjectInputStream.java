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
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

/** {@link ObjectInputStream} subclass that resolves class names using a specified class loader. */
final class ConfigurableObjectInputStream extends ObjectInputStream {
    private final ClassLoader classLoader;

    /**
     * Constructor.
     *
     * @param in the input stream to read from
     * @param classLoader the class loader to load classes from
     * @throws IOException if an I/O error occurs while reading the stream header
     */
    ConfigurableObjectInputStream(InputStream in, ClassLoader classLoader) throws IOException {
        super(in);
        this.classLoader = classLoader;
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc)
            throws IOException, ClassNotFoundException {
        // Note: we can't use ClassLoader#loadClass here because the name may denote an array class
        return Class.forName(desc.getName(), false, classLoader);
    }
}
