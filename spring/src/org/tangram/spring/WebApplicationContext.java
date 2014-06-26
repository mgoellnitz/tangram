/**
 *
 * Copyright 2011-2014 Martin Goellnitz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.tangram.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 *  Test tool for spring startup performance.
 */
public class WebApplicationContext extends XmlWebApplicationContext {

    private static final Logger LOG = LoggerFactory.getLogger(WebApplicationContext.class);


    /**
     * Finish the initialization of this context's bean factory, initializing all remaining singleton beans.
     */
    @Override
    protected void finishBeanFactoryInitialization(ConfigurableListableBeanFactory beanFactory) {
        if (LOG.isInfoEnabled()) {
            LOG.info("finishBeanFactoryInitialization() start");
        } // if
        // Initialize conversion service for this context.
        if (beanFactory.containsBean(CONVERSION_SERVICE_BEAN_NAME)
                &&beanFactory.isTypeMatch(CONVERSION_SERVICE_BEAN_NAME, ConversionService.class)) {
            beanFactory
                    .setConversionService(beanFactory.getBean(CONVERSION_SERVICE_BEAN_NAME, ConversionService.class));
        }

        // Stop using the temporary ClassLoader for type matching.
        beanFactory.setTempClassLoader(null);

        // Allow for caching all bean definition metadata, not expecting further changes.
        beanFactory.freezeConfiguration();

        if (LOG.isInfoEnabled()) {
            LOG.info("finishBeanFactoryInitialization() "+beanFactory.getClass().getName());
        } // if
        // Instantiate all remaining (non-lazy-init) singletons.
        beanFactory.preInstantiateSingletons();
        if (LOG.isInfoEnabled()) {
            LOG.info("finishBeanFactoryInitialization() end");
        } // if
    } // finishBeanFactoryInitialization()

} // WebApplicationContext
