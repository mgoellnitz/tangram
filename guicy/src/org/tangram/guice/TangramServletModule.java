/**
 *
 * Copyright 2015 Martin Goellnitz
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

import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;
import groovy.lang.Binding;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.inject.Named;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.controller.ControllerHook;
import org.tangram.util.SetupUtils;
import org.tangram.view.TemplateResolver;


/**
 * All elements generically needed for tangram in guice scanerios are collected here.
 *
 * Subclasses implement the createRestartCache() and createBeanFactor() methods and a potentially empty
 * appendConfiguration() which gets called at the end of the generic configureServlets() method.
 */
public class TangramServletModule extends ServletModule {

    private static final Logger LOG = LoggerFactory.getLogger(TangramServletModule.class);

    private final Properties configuration = new Properties();

    @SuppressWarnings("rawtypes")
    protected static final Key<Map> VIEW_SETTINGS_KEY = Key.get(Map.class, new Named() {

        @Override
        public String value() {
            return "viewSettings";
        }


        @Override
        public Class<? extends Annotation> annotationType() {
            return Named.class;
        }

    });

    private final Set<ControllerHook> controllerHooks = new HashSet<>();

    private final Set<TemplateResolver<String>> templateResolvers = new HashSet<>();

    private Multibinder<ControllerHook> hookBinder = null;

    @SuppressWarnings("rawtypes")
    private Multibinder<TemplateResolver> resolverBinder = null;


    public void addControllerHook(ControllerHook hook) {
        controllerHooks.add(hook);
        hookBinder.addBinding().toInstance(hook);
    } // addControllerHook()


    public void addTemplateResolver(TemplateResolver<String> resolver) {
        templateResolvers.add(resolver);
        resolverBinder.addBinding().toInstance(resolver);
    } // addTemplateResolver()


    /**
     * Producer to find all controller hook instances in the system.
     *
     * @return collection of controller hooks
     */
    @Provides
    public Collection<ControllerHook> getControllerHooks() {
        return controllerHooks;
    } // getControllerHooks()


    /**
     * Producer to find all template resolver instances in the system.
     *
     * @return collection of template resolvers
     */
    @Provides
    public Set<TemplateResolver<String>> getTemplateResolvers() {
        return templateResolvers;
    } // getTemplateResolvers()


    private CompilerConfiguration getCompilerConfiguration() {
        return new CompilerConfiguration();
    }


    private GroovyShell createShell() {
        return new GroovyShell(Thread.currentThread().getContextClassLoader(), new Binding(), getCompilerConfiguration());
    }


    @Override
    protected final void configureServlets() {
        LOG.info("configureServlets() reading configuration");
        try {
            configuration.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("guice/defaults.properties"));
        } catch (Exception ex) {
            LOG.error("configureServlets() could not read config defaults: {}", ex.getMessage());
        } // try/catch
        try {
            configuration.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("guice/tangram.properties"));
        } catch (Exception ex) {
            LOG.error("configureServlets() could not read configuration: {}", ex.getMessage());
        } // try/catch
        Names.bindProperties(binder(), configuration);

        hookBinder = Multibinder.newSetBinder(binder(), ControllerHook.class);
        resolverBinder = Multibinder.newSetBinder(binder(), TemplateResolver.class);

        String dispatcherPath = getServletContext().getInitParameter(DefaultTangramContextListener.DISPATCHER_PATH);
        dispatcherPath = dispatcherPath==null ? "/s" : dispatcherPath;
        configuration.setProperty("dispatcherPath", dispatcherPath);

        final GroovyShell shell = createShell();
        Set<String> scripts = new HashSet<>();
        try {
            scripts = SetupUtils.getResourceListing("guice", ".groovy");
        } catch (Exception e) {
            LOG.error("{} error while reading all modules binding scripts", e);
        } // try/catch

        for (final String name : scripts) {
            try {
                LOG.info("configure() loading "+name);
                URL resource = Thread.currentThread().getContextClassLoader().getResource(name);
                LOG.info("configure() loading "+resource);
                Script s = shell.parse(new GroovyCodeSource(resource));
                s.setProperty("config", configuration);
                s.setProperty("module", this);
                s.setProperty("log", LoggerFactory.getLogger(name.replace('/', '.')));
                s.run();
            } catch (IOException e) {
                throw new Error("Failed to configure module via "+name, e);
            } // try/catch
        }
        LOG.info("configureServlets() configuration done");
    } // configureServlets()

} // TangramServletModule
