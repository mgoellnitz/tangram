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
import org.tangram.util.SetupUtils
import org.tangram.PersistentRestartCache
import org.tangram.view.PropertyConverter
import org.tangram.components.gae.GaeLoginSupport
import org.tangram.security.LoginSupport
import org.tangram.servlet.MeasureTimeFilter
import org.tangram.servlet.PasswordFilter
import org.tangram.content.BeanFactory
import org.tangram.mutable.MutableBeanFactory
import org.tangram.jdo.JdoBeanFactory
import org.tangram.gae.GaeBeanFactory
import org.tangram.gae.GaePropertyConverter
import org.tangram.components.gae.GaeCacheAdapter

log.info "starting"
String dispatcherPath = config.getProperty("dispatcherPath", "/s")

log.info("configuring persistent restart cache")
module.bind(PersistentRestartCache.class).toInstance(new GaeCacheAdapter())

log.info "configuring property converter"
module.bind(PropertyConverter.class).toInstance(new GaePropertyConverter())

log.info "configuring bean factory"
BeanFactory beanFactory = new GaeBeanFactory()
module.getServletContext().setAttribute(Constants.ATTRIBUTE_BEAN_FACTORY, beanFactory);
module.bind(BeanFactory.class).toInstance(beanFactory)
module.bind(MutableBeanFactory.class).toInstance(beanFactory)
module.bind(JdoBeanFactory.class).toInstance(beanFactory);

log.info("configuring login support")
LoginSupport loginSupport = new GaeLoginSupport()
String admins = config.getProperty("adminUsers", "")
Set<String> adminUsers = SetupUtils.stringSetFromParameterString(admins)
String users = config.getProperty("allowedUsers", "")
Set<String> allowedUsers = SetupUtils.stringSetFromParameterString(users)
loginSupport.setAdminUsers(adminUsers)
loginSupport.setAllowedUsers(allowedUsers)
module.bind(LoginSupport.class).toInstance(loginSupport)

PasswordFilter passwordFilter = new PasswordFilter()
log.info("configureServlets() password filter {} for {}", passwordFilter, dispatcherPath)
passwordFilter.setLoginSupport(loginSupport)
module.filter(dispatcherPath+"/*").through(passwordFilter)

log.info("configureServlets() measure time filter for {}", dispatcherPath)
module.filter(dispatcherPath+"/*").through(new MeasureTimeFilter())

log.info "done."
println "gae done."
