/**
 *
 * Copyright 2013-2014 Martin Goellnitz
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
package org.tangram.components.spring;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.convert.ConversionService;


@Named
@Singleton
public final class TangramSpringServices implements ApplicationContextAware {

    private static final Logger LOG = LoggerFactory.getLogger(TangramSpringServices.class);

    private static ApplicationContext applicationContext;

    private static ConversionService conversionService = null;


    private TangramSpringServices() {
    }


    private static class ConversionServiceHolder {

        public static final ConversionService INSTANCE = getBeanFromContext(ConversionService.class);

    }


    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }


    @Inject
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        TangramSpringServices.applicationContext = applicationContext;
    }


    public static <T extends Object> T getBeanFromContext(Class<? extends T> cls) {
        ApplicationContext appContext = getApplicationContext();
        T result = (appContext!=null) ? appContext.getBean(cls) : null;
        if (result==null) {
            throw new RuntimeException("getBeanFromContext() no item of type "+cls.getName()+" available.");
        } // if
        return result;
    } // getBeanFromContext()


    public static <T extends Object> T getBeanFromContext(Class<? extends T> cls, String name) {
        ApplicationContext appContext = getApplicationContext();
        T result = (appContext!=null) ? appContext.getBean(name, cls) : null;
        if (result==null) {
            throw new RuntimeException("getBeanFromContext() no item of type "+cls.getName()+" and Name "+name+" available.");
        } // if
        return result;
    } // getBeanFromContext()


    /**
     * Returns the system wide conversation service instance.
     * This more or less is an implementation of @Autowired(required=false).
     *
     * @return May return null,
     */
    public static ConversionService getConversionService() {
        return ConversionServiceHolder.INSTANCE;
    } // getConversionService()


    /**
     * Create a bean wrapper instance from a bean object and prepare it with a conversion service if available.
     *
     * @param bean any object to be wrapped for java bean access
     * @return wrapper for the given bean
     */
    public static BeanWrapper createWrapper(Object bean) {
        BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(bean);
        try {
            ConversionService converter = TangramSpringServices.getConversionService();
            if (converter!=null) {
                wrapper.setConversionService(converter);
            } // if
            LOG.info("createWrapper() conversion service {}", wrapper.getConversionService());
        } catch (Exception e) {
            // This is not an error since conversion services are optional.
            LOG.warn("createWrapper()", e);
        } // try/catch
        return wrapper;
    } // createWrapper()

} // TangramSpringServices
