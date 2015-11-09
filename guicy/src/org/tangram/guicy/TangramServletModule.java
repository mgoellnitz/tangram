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
package org.tangram.guicy;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;
import groovy.lang.Binding;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.inject.Named;
import javax.servlet.ServletContext;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.pac4j.core.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.controller.ControllerHook;
import org.tangram.util.SystemUtils;
import org.tangram.view.TemplateResolver;


/**
 * All elements generically needed for tangram in guice scenarios are collected here.
 *
 * This class calls any *.groovy script in the guicy/ resource folder with its self bound to the name module,
 * a decent log bound to the name log and the net configuration as properties loaded from "/guicy/defaults.properties"
 * followed by "/guicy/tangram.properties".
 */
public class TangramServletModule extends ServletModule {

    /**
     * Servlet context init parameter indicating the base URI path for the tangram dispatcher.
     */
    public static final String DISPATCHER_PATH = "tangram.dispatcher.path";

    /**
     * Resource base path for the configuration of the Google Guice integration.
     */
    public static final String GUICY_BASE = "guicy";

    /**
     * Resource name of a properties file for additional values to be used during configuration.
     */
    public static final String GUICY_PROPERTIES = GUICY_BASE+"/tangram.properties";

    /**
     * Resource name of a properties file for defaults of additional values (s.a.) to be used during configuration.
     */
    public static final String GUICY_DEFAULTS = GUICY_BASE+"/defaults.properties";

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

    private Multibinder<ControllerHook> controllerHookBinder = null;

    @SuppressWarnings("rawtypes")
    private Multibinder<Client> clientBinder = null;

    @SuppressWarnings("rawtypes")
    private Multibinder<TemplateResolver> resolverBinder = null;


    public void addControllerHook(ControllerHook hook) {
        controllerHookBinder.addBinding().toInstance(hook);
    } // addControllerHook()


    public void addTemplateResolver(TemplateResolver<String> resolver) {
        resolverBinder.addBinding().toInstance(resolver);
    } // addTemplateResolver()


    public void addClient(Client<?, ?> client) {
        clientBinder.addBinding().toInstance(client);
    } // addTemplateResolver()


    /**
     * Obtain Type Literal descriptor for Map&lt;String, String&gt;.
     *
     * @return type literal instance
     * @throws NoSuchFieldException should in fact not happen
     */
    public TypeLiteral<?> getStringStringMap() throws NoSuchFieldException {
        Object mapVehicle = new Object() {
            Map<String, String> map;
        };
        Type interimType = mapVehicle.getClass().getDeclaredField("map").getGenericType();
        return TypeLiteral.get(interimType);
    } // getStringStringMap()


    /**
     * Create a groovy shell with our current class loader.
     *
     * @return groovy shell instance
     */
    private GroovyShell createShell() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return new GroovyShell(classLoader, new Binding(), new CompilerConfiguration());
    } // createShell()


    @Override
    protected final void configureServlets() {
        LOG.info("configureServlets() reading configuration");
        try {
            configuration.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(GUICY_DEFAULTS));
        } catch (Exception ex) {
            LOG.error("configureServlets() could not read config defaults: {}", ex.getMessage());
        } // try/catch
        try {
            configuration.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(GUICY_PROPERTIES));
        } catch (Exception ex) {
            LOG.error("configureServlets() could not read configuration: {}", ex.getMessage());
        } // try/catch
        Names.bindProperties(binder(), configuration);

        controllerHookBinder = Multibinder.newSetBinder(binder(), ControllerHook.class);
        resolverBinder = Multibinder.newSetBinder(binder(), TemplateResolver.class);
        clientBinder = Multibinder.newSetBinder(binder(), Client.class);

        ServletContext context = getServletContext();
        String dispatcherPath = context==null ? null : context.getInitParameter(DISPATCHER_PATH);
        dispatcherPath = dispatcherPath==null ? "/s" : dispatcherPath;
        configuration.setProperty("dispatcherPath", dispatcherPath);

        final GroovyShell shell = createShell();
        Set<String> scripts = new HashSet<>();
        try {
            scripts = SystemUtils.getResourceListing(GUICY_BASE, ".groovy");
        } catch (Exception e) {
            LOG.error("{} error while reading all modules binding scripts", e);
        } // try/catch

        for (final String name : scripts) {
            try {
                LOG.info("configureServlets() loading "+name);
                URL resource = Thread.currentThread().getContextClassLoader().getResource(name);
                LOG.info("configureServlets() loading "+resource);
                Script s = shell.parse(new GroovyCodeSource(resource));
                s.setProperty("config", configuration);
                s.setProperty("module", this);
                s.setProperty("log", LoggerFactory.getLogger(name.replace('.', '_').replace('/', '.')));
                s.run();
            } catch (IOException e) {
                throw new Error("Failed to configure module via "+name, e);
            } // try/catch
        }
        LOG.info("configureServlets() configuration done");
    } // configureServlets()

} // TangramServletModule
