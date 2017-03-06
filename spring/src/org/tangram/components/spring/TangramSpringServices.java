/**
 *
 * Copyright 2013-2017 Martin Goellnitz
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
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


@Named
@Singleton
public final class TangramSpringServices implements ApplicationContextAware {

    private static ApplicationContext applicationContext;


    private TangramSpringServices() {
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

} // TangramSpringServices
