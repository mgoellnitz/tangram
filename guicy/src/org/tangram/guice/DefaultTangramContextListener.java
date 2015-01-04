/**
 *
 * Copyright 2013-2015 Martin Goellnitz
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
package org.tangram.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.mycila.guice.ext.closeable.CloseableModule;
import com.mycila.guice.ext.jsr250.Jsr250Module;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.guice.web.ShiroWebModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Servlet context listener to integrate customizable guice servlet modules and optional shiro web modules via
 * web.xml configuation.
 */
public class DefaultTangramContextListener extends GuiceServletContextListener {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultTangramContextListener.class);

    public static final String SHIRO_WEB_MODULE_CLASS = "shiro.web.module.class";

    public static final String SERVLET_MODULE_CLASS = "servlet.module.class";

    public static final String DISPATCHER_PATH = "tangram.dispatcher.path";

    private ShiroWebModule shiroWebModule = null;

    private ServletModule servletModule = null;


    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        LOG.info("contextInitialized()");
        ServletContext context = servletContextEvent.getServletContext();
        String webModuleClassName = context.getInitParameter(SHIRO_WEB_MODULE_CLASS);
        String dispatcherPath = context.getInitParameter(DISPATCHER_PATH);
        dispatcherPath = dispatcherPath == null ? "/s" : dispatcherPath;
        if (StringUtils.isNotBlank(webModuleClassName)) {
            try {
                Class<?> forName = Class.forName(webModuleClassName);
                Object[] args = {context, dispatcherPath};
                shiroWebModule = (ShiroWebModule) forName.getConstructors()[0].newInstance(args);
            } catch (Exception e) {
                LOG.error("contextInitialized() cannot obtain shiro web module", e);
            } // try/catch
        } // if
        LOG.info("contextInitialized() shiro web module: {} :{}", shiroWebModule, webModuleClassName);

        String servletModuleClassName = context.getInitParameter(SERVLET_MODULE_CLASS);
        if (StringUtils.isNotBlank(servletModuleClassName)) {
            try {
                Class<?> forName = Class.forName(servletModuleClassName);
                servletModule = (ServletModule) forName.getConstructors()[0].newInstance(new Object[0]);
            } catch (Exception e) {
                LOG.error("contextInitialized() cannot obtain servlet module", e);
            } // try/catch
        } // if
        servletModule = servletModule==null ? new DefaultTangramServletModule() : servletModule;
        LOG.info("contextInitialized() servlet module: {} :{}", servletModule, servletModuleClassName);

        super.contextInitialized(servletContextEvent);
    } // contextInitialized()


    @Override
    protected Injector getInjector() {
        LOG.info("getInjector()");
        Injector injector;
        if (shiroWebModule==null) {
            injector = Guice.createInjector(new CloseableModule(), new Jsr250Module(), servletModule);
        } else {
            injector = Guice.createInjector(new CloseableModule(), new Jsr250Module(), shiroWebModule, BasicShiroWebModule.guiceFilterModule(), servletModule);
        } // if
        return injector;
    } // getInjector()

} // DefaultTangramContextListener
