/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2012 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tangram.guicy.postconstruct;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import java.lang.reflect.Method;
import javax.annotation.PostConstruct;


/**
 * Add {@link PostConstruct} annotation support from JSR250 to Google Guice.
 */
public class PostConstructModule extends AbstractModule {

    // private static final Logger LOG = LoggerFactory.getLogger(PostConstructModule.class);

    
    @Override
    protected void configure() {
        bindListener(Matchers.any(), new TypeListener() {
            @Override
            public <I> void hear(TypeLiteral<I> injectableType, TypeEncounter<I> encounter) {
                Class<? super I> type = injectableType.getRawType();
                while (type!=null) {
                    Method[] methods = type.getDeclaredMethods();
                    for (final Method method : methods) {
                        PostConstruct postConstruct = method.getAnnotation(PostConstruct.class);
                        if (postConstruct!=null) {
                            // Note: results in a PMD warning due to access of 'method'
                            // LOG.info("hear() found post construct method {}", method);
                            OnInjectionInvoker<I> callback = new OnInjectionInvoker<I>(method);
                            encounter.register(callback);
                        }
                    }

                    type = type.getSuperclass();
                }
            }
        });
    }


    /**
     * Implement hashCode() and equals() such that two instances of the module will be equal.
     */
    @Override
    public int hashCode() {
        return this.getClass().hashCode();
    }


    @Override
    public boolean equals(Object o) {
        return (o!=null)&&this.getClass().equals(o.getClass());
    }

} // PostConstructModule
