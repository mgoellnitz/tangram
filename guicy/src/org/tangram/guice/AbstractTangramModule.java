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
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.Constants;
import org.tangram.PersistentRestartCache;
import org.tangram.components.CodeExporter;
import org.tangram.components.CodeResourceCache;
import org.tangram.components.GroovyClassRepository;
import org.tangram.components.MetaLinkHandler;
import org.tangram.components.SimpleStatistics;
import org.tangram.components.StatisticsHandler;
import org.tangram.components.mutable.ToolHandler;
import org.tangram.components.servlet.ServletViewUtilities;
import org.tangram.content.BeanFactory;
import org.tangram.controller.ControllerHook;
import static org.tangram.guice.AbstractTangramModule.VIEW_SETTINGS_KEY;
import org.tangram.link.GenericLinkFactoryAggregator;
import org.tangram.link.LinkFactoryAggregator;
import org.tangram.link.LinkHandlerRegistry;
import org.tangram.logic.ClassRepository;
import org.tangram.monitor.Statistics;
import org.tangram.mutable.MutableBeanFactory;
import org.tangram.servlet.DefaultServlet;
import org.tangram.servlet.JspTemplateResolver;
import org.tangram.servlet.MetaServlet;
import org.tangram.servlet.RepositoryTemplateResolver;
import org.tangram.view.DynamicViewContextFactory;
import org.tangram.view.TemplateResolver;
import org.tangram.view.ViewContextFactory;
import org.tangram.view.ViewUtilities;
import org.tangram.view.velocity.VelocityPatchBean;


/**
 * All elements generically needed for tangram in guice scanerios are collected here.
 * 
 * Subclasses implement the createRestartCache() and createBeanFactor() methods and a potentially empty 
 * appendConfiguration() which gets called at the end of the generic configureServlets() method.
 */
public abstract class AbstractTangramModule extends ServletModule {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractTangramModule.class);

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


    public void addControllerHook(ControllerHook hook) {
        controllerHooks.add(hook);
    } // addControllerHook()


    public void addTemplateResolver(TemplateResolver<String> resolver) {
        templateResolvers.add(resolver);
    } // addTemplateResolver()


    /**
     * Producer to find all controller hook instances in the system.
     *
     * @return
     */
    @Provides
    public Collection<ControllerHook> getControllerHooks() {
        LOG.info("getControllerHooks() "+controllerHooks);
        return controllerHooks;
    } // getControllerHooks()


    /**
     * Producer to find all template resolver instances in the system.
     *
     * @return
     */
    @Provides
    public Set<TemplateResolver<String>> getTemplateResolvers() {
        LOG.info("getTemplateResolvers() "+templateResolvers);
        return templateResolvers;
    } // getTemplateResolvers()


    /**
     * return string configuration value from underlying configuration map.
     *
     * @param key key to the underlying configuration map
     * @param defaultValue default value to be returned if the given key is not found
     * @return key's associated value or default
     */
    protected String getConfigValue(String key, String defaultValue) {
        return configuration.getProperty(key, defaultValue);
    } // getConfigValue()


    /**
     * Every module has to provide a restart cache.
     *
     * the result might well be the dummy implementation.
     *
     * @return persistent restart cache instance to be used throughout the application
     */
    protected abstract PersistentRestartCache createRestartCache();


    /**
     * Every module has to provide a bean factory implementation.
     *
     * In this case we also assume that you will be using a mutable one.
     *
     * @return bean factory instance to be used throughout the application
     */
    protected abstract MutableBeanFactory createBeanFactory();


    /**
     * Abstract hook to generically extend the tangram module setup after the basics have been integrated
     * 
     * @param dispatcherPath base path for the tangram meta and default servlet
     */
    protected abstract void appendConfiguration(String dispatcherPath);


    @Override
    protected final void configureServlets() {
        LOG.info("configureServlets() reading configuration");
        try {
            configuration.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("guice/defaults.properties"));
        } catch (IOException ex) {
            LOG.error("configureServlets()", ex);
        } // try/catch
        try {
            configuration.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("guice/tangram.properties"));
        } catch (IOException ex) {
            LOG.error("configureServlets()", ex);
        } // try/catch
        Names.bindProperties(binder(), configuration);

        String dispatcherPath = getServletContext().getInitParameter(DefaultTangramContextListener.DISPATCHER_PATH);
        dispatcherPath = dispatcherPath==null ? "/s" : dispatcherPath;
        configuration.setProperty("dispatcherPath", dispatcherPath);

        LOG.info("configureServlets() view settings");
        Map<String, Object> viewSettings = new HashMap<>();
        viewSettings.put("cssCacheTime", configuration.getProperty("cssCacheTime", "10080"));
        viewSettings.put("jsCacheTime", configuration.getProperty("jsCacheTime", "10080"));
        viewSettings.put("imageCacheTime", configuration.getProperty("imageCacheTime", "10080"));
        getServletContext().setAttribute(Constants.ATTRIBUTE_VIEW_SETTINGS, viewSettings);
        bind(VIEW_SETTINGS_KEY).toInstance(viewSettings);

        LOG.info("configureServlets() statistics");
        Statistics statistics = new SimpleStatistics();
        getServletContext().setAttribute(Constants.ATTRIBUTE_STATISTICS, statistics);
        bind(Statistics.class).toInstance(statistics);

        LOG.info("configureServlets() persistent restart cache");
        PersistentRestartCache persistentRestartCache = createRestartCache();
        bind(PersistentRestartCache.class).toInstance(persistentRestartCache);

        LOG.warn("configureServlets() bean factory");
        MutableBeanFactory beanFactory = createBeanFactory();
        bind(BeanFactory.class).toInstance(beanFactory);
        bind(MutableBeanFactory.class).toInstance(beanFactory);

        LOG.info("configureServlets() code resource cache");
        CodeResourceCache codeResourceCache = new CodeResourceCache();
        bind(CodeResourceCache.class).toInstance(codeResourceCache);

        LOG.info("configureServlets() velocity patch bean");
        VelocityPatchBean velocityPatchBean = new VelocityPatchBean();
        velocityPatchBean.setCodeResourceCache(codeResourceCache);

        LOG.info("configureServlets() class repository");
        GroovyClassRepository classRepository = new GroovyClassRepository();
        bind(ClassRepository.class).toInstance(classRepository);

        LOG.info("configureServlets() view context factory");
        ViewContextFactory viewContextFactory = new DynamicViewContextFactory();
        bind(ViewContextFactory.class).toInstance(viewContextFactory);

        LOG.info("configureServlets() link factory aggregator");
        GenericLinkFactoryAggregator linkFactoryAggregator = new GenericLinkFactoryAggregator();
        linkFactoryAggregator.setDispatcherPath(dispatcherPath);
        getServletContext().setAttribute(Constants.ATTRIBUTE_LINK_FACTORY_AGGREGATOR, linkFactoryAggregator);
        bind(LinkFactoryAggregator.class).toInstance(linkFactoryAggregator);

        LOG.info("configureServlets() view utilities");
        ServletViewUtilities viewUtilities = new ServletViewUtilities();
        getServletContext().setAttribute(Constants.ATTRIBUTE_VIEW_UTILITIES, viewUtilities);
        bind(ViewUtilities.class).toInstance(viewUtilities);

        LOG.info("configureServlets() link handler registry");
        MetaLinkHandler metaLinkHandler = new MetaLinkHandler();
        bind(LinkHandlerRegistry.class).toInstance(metaLinkHandler);
        bind(MetaLinkHandler.class).toInstance(metaLinkHandler);

        LOG.info("configureServlets() statistics handler");
        StatisticsHandler statisticsHandler = new StatisticsHandler();
        bind(StatisticsHandler.class).toInstance(statisticsHandler);

        LOG.info("configureServlets() jsp templating");
        JspTemplateResolver jspTemplateResolver = new JspTemplateResolver();
        // TODO: must be configurable
        jspTemplateResolver.setActivateCaching(true);
        jspTemplateResolver.setName("JSP");
        // These are only the defaults from the underlying class:
        jspTemplateResolver.setPrefix("/WEB-INF/view/jsp/");
        jspTemplateResolver.setSuffix(".jsp");

        LOG.info("configureServlets() velocity templating");
        RepositoryTemplateResolver repositoryTemplateResolver = new RepositoryTemplateResolver();
        repositoryTemplateResolver.setActivateCaching(true);
        repositoryTemplateResolver.setName("Velocity");

        addTemplateResolver(jspTemplateResolver);
        addTemplateResolver(repositoryTemplateResolver);

        @SuppressWarnings("rawtypes")
        Multibinder<TemplateResolver> resolverBinder = Multibinder.newSetBinder(binder(), TemplateResolver.class);
        resolverBinder.addBinding().toInstance(jspTemplateResolver);

        LOG.info("configureServlets() code exporter handler");
        CodeExporter codeExporter = new CodeExporter();
        bind(CodeExporter.class).toInstance(codeExporter);

        LOG.info("configureServlets() tool handler");
        ToolHandler tool = new ToolHandler();
        bind(ToolHandler.class).toInstance(tool);
        
        LOG.info("configureServlets() default servlet");
        DefaultServlet tangramDefaultServlet = new DefaultServlet();
        serveRegex("\\"+dispatcherPath+"\\/id_([A-Z][a-zA-Z]+:[0-9]+)\\/view_(.*)").with(tangramDefaultServlet);
        serveRegex("\\"+dispatcherPath+"\\/id_([A-Z][a-zA-Z]+:[0-9]+)").with(tangramDefaultServlet);

        LOG.info("configureServlets() meta servlet");
        MetaServlet tangramMetaServlet = new MetaServlet();
        serve(dispatcherPath+"/*").with(tangramMetaServlet);

        LOG.info("configureServlets() default configuration done");
        appendConfiguration(dispatcherPath);
    } // configureServlets()

} // AbstractTangramModule
