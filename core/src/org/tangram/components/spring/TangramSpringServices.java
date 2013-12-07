/**
 *
 * Copyright 2013 Martin Goellnitz
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.convert.ConversionService;
import org.tangram.spring.view.ViewHandler;


@Named
public class TangramSpringServices implements ApplicationContextAware {

    private static Log log = LogFactory.getLog(TangramSpringServices.class);

    private static ApplicationContext applicationContext;

    private static ViewHandler viewHandler = null;

    private static ConversionService conversionService = null;


    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }


    @Inject
    public void setApplicationContext(ApplicationContext applicationContext) {
        TangramSpringServices.applicationContext = applicationContext;
    }


    public static ViewHandler getViewHandler() {
        return viewHandler;
    }


    @Inject
    public void setViewHandler(ViewHandler viewHandler) {
        TangramSpringServices.viewHandler = viewHandler;
    }


    public static <T extends Object> T getBeanFromContext(Class<? extends T> cls) {
        T result = null;
        ApplicationContext appContext = getApplicationContext();
        if (appContext!=null) {
            result = appContext.getBean(cls);
        } // if
        if (result==null) {
            throw new RuntimeException("getBeanFromContext() no item of type "+cls.getName()+" available.");
        } // if
        return result;
    } // getBeanFromContext()


    public static <T extends Object> T getBeanFromContext(Class<? extends T> cls, String name) {
        T result = null;
        ApplicationContext appContext = getApplicationContext();
        if (appContext!=null) {
            result = appContext.getBean(name, cls);
        } // if
        if (result==null) {
            throw new RuntimeException("getBeanFromContext() no item of type "+cls.getName()+" and Name "+name+" available.");
        } // if
        return result;
    } // getBeanFromContext()


    /**
     * this more or less is an implementation of @Autowired(required=false).
     */
    public static ConversionService getConversionService() {
        if (conversionService==null) {
            conversionService = getBeanFromContext(ConversionService.class);
        } // if
        return conversionService;
    }


    /**
     * create a bean wrapper instance from a bean object and prepare it with a conversion service if available.
     *
     * @param bean
     * @param conversionService
     * @return
     * public static Bean Wrapper createWrapper(Object bean) {
     * Bean Wrapper wrapper;
     * wrapper = PropertyAccessorFactory.forBeanPropertyAccess(bean);
     * try {
     * ConversionService converter = TangramSpringServices.getConversionService();
     * if (converter!=null) {
     * wrapper.setConversionService(converter);
     * } // if
     * if (log.isInfoEnabled()) {
     * log.info("createWrapper() conversion service "+wrapper.getConversionService());
     * } // if
     * } catch (Exception e) {
     * // conversion services are still optional for some time
     * } // try/catch
     * return wrapper;
     * }
     */

} // TangramSpringServices
