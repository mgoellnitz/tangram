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
import org.tangram.Constants
import org.tangram.components.CodeExporter
import org.tangram.components.CodeResourceCache
import org.tangram.components.GroovyClassRepository
import org.tangram.components.MetaLinkHandler
import org.tangram.components.SimpleStatistics
import org.tangram.components.StatisticsHandler
import org.tangram.components.servlet.ServletViewUtilities
import org.tangram.link.GenericLinkFactoryAggregator
import org.tangram.link.LinkFactoryAggregator
import org.tangram.link.LinkHandlerRegistry
import org.tangram.logic.ClassRepository
import org.tangram.monitor.Statistics
import org.tangram.servlet.DefaultServlet
import org.tangram.servlet.JspTemplateResolver
import org.tangram.servlet.MetaServlet
import org.tangram.servlet.RepositoryTemplateResolver
import org.tangram.view.DynamicViewContextFactory
import org.tangram.view.ViewContextFactory
import org.tangram.view.ViewUtilities
import org.tangram.view.velocity.VelocityPatchBean

log.info "starting"
String dispatcherPath = config.getProperty("dispatcherPath", "/s")

log.info("configuring view settings")
Map<String, Object> viewSettings = new HashMap<>()
viewSettings.put("cssCacheTime", config.getProperty("cssCacheTime", "10080"))
viewSettings.put("jsCacheTime", config.getProperty("jsCacheTime", "10080"))
viewSettings.put("imageCacheTime", config.getProperty("imageCacheTime", "10080"))
module.getServletContext().setAttribute(Constants.ATTRIBUTE_VIEW_SETTINGS, viewSettings)
module.bind(module.VIEW_SETTINGS_KEY).toInstance(viewSettings)

log.info("configuring statistics")
Statistics statistics = new SimpleStatistics()
module.getServletContext().setAttribute(Constants.ATTRIBUTE_STATISTICS, statistics)
module.bind(Statistics.class).toInstance(statistics)

log.info("configuring code resource cache")
CodeResourceCache codeResourceCache = new CodeResourceCache()
module.bind(CodeResourceCache.class).toInstance(codeResourceCache)

log.info("configuring velocity patch bean")
VelocityPatchBean velocityPatchBean = new VelocityPatchBean()
velocityPatchBean.setCodeResourceCache(codeResourceCache)

log.info("configuring link factory aggregator")
LinkFactoryAggregator linkFactoryAggregator = new GenericLinkFactoryAggregator()
linkFactoryAggregator.setDispatcherPath(dispatcherPath)
module.getServletContext().setAttribute(Constants.ATTRIBUTE_LINK_FACTORY_AGGREGATOR, linkFactoryAggregator)
module.bind(LinkFactoryAggregator.class).toInstance(linkFactoryAggregator)

log.info("configuring statistics handler")
module.bind(StatisticsHandler.class).toInstance(new StatisticsHandler())

log.info("configuring code exporter handler")
module.bind(CodeExporter.class).toInstance(new CodeExporter())

log.info("configuring class repository")
module.bind(ClassRepository.class).toInstance(new GroovyClassRepository())

log.info("configuring code exporter handler")
module.bind(ViewContextFactory.class).toInstance(new DynamicViewContextFactory())

log.info("configuring view utilities");
ViewUtilities viewUtilities = new ServletViewUtilities()
module.getServletContext().setAttribute(Constants.ATTRIBUTE_VIEW_UTILITIES, viewUtilities)
module.bind(ViewUtilities.class).toInstance(viewUtilities)

log.info("configuring link handler registry")
MetaLinkHandler metaLinkHandler = new MetaLinkHandler()
module.bind(LinkHandlerRegistry.class).toInstance(metaLinkHandler)
module.bind(MetaLinkHandler.class).toInstance(metaLinkHandler)

log.info("configuring jsp templating")
JspTemplateResolver jspTemplateResolver = new JspTemplateResolver()
jspTemplateResolver.setName("JSP")
jspTemplateResolver.setActivateCaching(true)
module.addTemplateResolver(jspTemplateResolver)

log.info("configuring velocity templating")
RepositoryTemplateResolver repositoryTemplateResolver = new RepositoryTemplateResolver()
repositoryTemplateResolver.setName("Velocity")
repositoryTemplateResolver.setActivateCaching(true)
module.addTemplateResolver(repositoryTemplateResolver)

log.info("configuring default servlet")
DefaultServlet tangramDefaultServlet = new DefaultServlet()
module.serveRegex("\\"+dispatcherPath+"\\/id_([A-Z][a-zA-Z]+:[0-9]+)\\/view_(.*)").with(tangramDefaultServlet)
module.serveRegex("\\"+dispatcherPath+"\\/id_([A-Z][a-zA-Z]+:[0-9]+)").with(tangramDefaultServlet)

log.info("configuring meta servlet");
MetaServlet tangramMetaServlet = new MetaServlet()
module.serve(dispatcherPath+"/*").with(tangramMetaServlet)

log.info "done."
println "tangram done."
