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

import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.PersistentRestartCache;
import org.tangram.components.ProtectionHook;
import org.tangram.components.editor.EditingHandler;
import org.tangram.controller.ControllerHook;
import org.tangram.controller.UniqueHostHook;
import org.tangram.mutable.AbstractMutableBeanFactory;
import org.tangram.mutable.MutableBeanFactory;
import org.tangram.security.GenericLoginSupport;
import org.tangram.security.LoginSupport;
import org.tangram.servlet.MeasureTimeFilter;
import org.tangram.servlet.PasswordFilter;
import org.tangram.util.FileRestartCache;
import org.tangram.util.StringUtil;
import org.tangram.view.GenericPropertyConverter;
import org.tangram.view.PropertyConverter;


/**
 * Default implementation of a guice servlet module.
 *
 * It supports scenarios where all codes except the data model are kept in the repository.
 * If you are really stuck to using guice you are strongly encouraged to have a custom module derived from
 * abstract tangram module.
 */
public class DefaultTangramServletModule extends AbstractTangramModule {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultTangramServletModule.class);


    protected PersistentRestartCache createRestartCache() {
        FileRestartCache persistentRestartCache = new FileRestartCache();
        persistentRestartCache.setFilename(getConfigValue("cacheFilename", "tangram-guice-cache.ser"));
        return persistentRestartCache;
    } // createRestartCache()


    protected MutableBeanFactory createBeanFactory() {
        // Should have used the interface here, but it does not contain setBasePackages
        // the main problem is not the interface but this class here so the hack should stay here.
        AbstractMutableBeanFactory beanFactory = null;
        String beanFactoryClassName = getConfigValue("beanFactory", "org.tangram.jpa.JpaBeanFactoryImpl");
        if (StringUtils.isNotBlank(beanFactoryClassName)) {
            LOG.info("createBeanFactory() instanciating {}", beanFactoryClassName);
            try {
                Class<?> forName = Class.forName(beanFactoryClassName);
                beanFactory = (AbstractMutableBeanFactory) forName.getConstructors()[0].newInstance(new Object[0]);
                Set<String> basePackages = StringUtil.stringSetFromParameterString(getConfigValue("basePackages", "org.tangram"));
                beanFactory.setBasePackages(basePackages);
            } catch (Exception e) {
                LOG.error("createBeanFactory() cannot obtain bean factory", e);
            } // try/catch
        } // if
        // Ugliest hack ever possible to invoke setConfigOverrides()
        for (Method method : beanFactory.getClass().getMethods()) {
            if (method.getName().equals("setConfigOverrides")) {
                Properties configOverrides = new Properties();
                try {
                    String overridesName = "guice/configOverrides.properties";
                    InputStream resource = Thread.currentThread().getContextClassLoader().getResourceAsStream(overridesName);
                    configOverrides.load(resource);
                    Object[] parameters = {configOverrides};
                    method.invoke(beanFactory, parameters);
                } catch (IllegalAccessException|IllegalArgumentException|InvocationTargetException|IOException ex) {
                    LOG.error("configureServlets()", ex);
                } // try/catch
            } // if
        } // for
        return beanFactory;
    } // createBeanFactory()


    @Override
    protected void appendConfiguration(String dispatcherPath) {
        bindConstant().annotatedWith(Names.named("shiro.loginUrl")).to("/shiro/login.jsp");
        bindConstant().annotatedWith(Names.named("shiro.successUrl")).to(dispatcherPath+"/list");

        ControllerHook uniqueHost = new UniqueHostHook();
        ControllerHook protectionHook = new ProtectionHook();
        addControllerHook(uniqueHost);
        addControllerHook(protectionHook);

        Multibinder<ControllerHook> hookBinder = Multibinder.newSetBinder(binder(), ControllerHook.class);
        hookBinder.addBinding().toInstance(uniqueHost);
        hookBinder.addBinding().toInstance(protectionHook);

        LOG.info("configureServlets() property converter");
        PropertyConverter propertyConverter = new GenericPropertyConverter();
        bind(PropertyConverter.class).toInstance(propertyConverter);

        LOG.info("configureServlets() login support");
        LoginSupport genericLoginSupport = new GenericLoginSupport();
        bind(LoginSupport.class).toInstance(genericLoginSupport);

        LOG.info("configureServlets() editing handler");
        EditingHandler editing = new EditingHandler();
        bind(EditingHandler.class).toInstance(editing);

        LOG.info("configureServlets() password filter");
        PasswordFilter passwordFilter = new PasswordFilter();
        passwordFilter.setAdminUsers(StringUtil.stringSetFromParameterString(getConfigValue("adminUsers", "")));
        passwordFilter.setLoginSupport(genericLoginSupport);
        filter(dispatcherPath+"/*").through(passwordFilter);

        LOG.info("configureServlets() measure time filter");
        filter(dispatcherPath+"/*").through(new MeasureTimeFilter());

        LOG.info("configureServlets() configuration done");
    } // appendConfiguration()

} // DefaultTangramServletModule
