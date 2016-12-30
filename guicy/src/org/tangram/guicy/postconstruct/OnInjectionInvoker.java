/*
 *
 * Copyright 2016 Martin Goellnitz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.tangram.guicy.postconstruct;

import com.google.inject.spi.InjectionListener;
import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Invoke a given method on injection at an objected just receiving injection.
 */
public class OnInjectionInvoker<I> implements InjectionListener<I> {

    private static final Logger LOG = LoggerFactory.getLogger(OnInjectionInvoker.class);

    private final Method method;


    public OnInjectionInvoker(Method method) {
        this.method = method;
    } // ()


    @Override
    public void afterInjection(I object) {
        LOG.info("afterInjection() {}@{}", method, object);
        try {
            method.setAccessible(true);
            method.invoke(object);
        } catch (Throwable t) {
            try {
                String message = t.getMessage();
                LOG.warn("afterInjection() {}@{}: {}", method, object, message);
            } catch (Exception e) {
                LOG.error("afterInjection()!", e);
            } // try/catch
        } // try/catch

    } // afterInjection()

} // OnInjectionInvoker
