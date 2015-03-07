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
import com.google.inject.name.Names;
import org.tangram.Constants
import org.tangram.util.SetupUtils
import org.tangram.PersistentRestartCache
import org.tangram.view.PropertyConverter
import org.tangram.servlet.MeasureTimeFilter
import org.tangram.servlet.PasswordFilter
import org.tangram.content.BeanFactory
import org.tangram.mutable.MutableBeanFactory
import org.tangram.jdo.JdoBeanFactory
import org.tangram.gae.GaeBeanFactory
import org.tangram.gae.GaePropertyConverter
import org.tangram.components.gae.GaeCacheAdapter
import org.pac4j.gae.client.GaeUserServiceClient

log.info "starting"
String dispatcherPath = config.getProperty("dispatcherPath", "/s")

log.info("configuring persistent restart cache")
module.bind(PersistentRestartCache.class).toInstance(new GaeCacheAdapter())

log.info "configuring property converter"
module.bind(PropertyConverter.class).toInstance(new GaePropertyConverter())

log.info "configuring bean factory"
BeanFactory beanFactory = new GaeBeanFactory()
module.getServletContext().setAttribute(Constants.ATTRIBUTE_BEAN_FACTORY, beanFactory)
module.bind(BeanFactory.class).toInstance(beanFactory)
module.bind(MutableBeanFactory.class).toInstance(beanFactory)
module.bind(JdoBeanFactory.class).toInstance(beanFactory)

log.info("configuring empty name password mapping")
Map<String,String> mapping = new HashMap<>()
// empty - which may be a bad decision as a non overridable instance
module.bind(module.stringStringMap).annotatedWith(Names.named("usernamePasswordMapping")).toInstance(mapping)

log.info("configuring authentication service")
GaeUserServiceClient gaeClient = new GaeUserServiceClient()
gaeClient.name='gae'
module.addClient(gaeClient)

log.info("configureServlets() password filter {} for {}", PasswordFilter.class, dispatcherPath)
module.filter(dispatcherPath+"/*").through(PasswordFilter.class)

log.info("configureServlets() measure time filter for {}", dispatcherPath)
module.filter(dispatcherPath+"/*").through(new MeasureTimeFilter())

log.info "done."
println "gae done."
