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
import org.tangram.components.nucleus.ClassRepositoryEnhancer
import org.tangram.util.SystemUtils
import org.tangram.view.PropertyConverter
import org.tangram.jdo.JdoBeanFactory
import org.tangram.nucleus.NucleusBeanFactory
import org.tangram.content.BeanFactory
import org.tangram.mutable.MutableBeanFactory
import org.tangram.view.GenericPropertyConverter

log.info "starting"

log.info "configuring property converter"
module.bind(PropertyConverter.class).toInstance(new GenericPropertyConverter())

log.info "configuring bean factory"
Set<String> basePackages = SystemUtils.stringSetFromParameterString(config.getProperty("basePackages", "org.tangram"))
NucleusBeanFactory beanFactory = new NucleusBeanFactory()
beanFactory.setBasePackages(basePackages)

String overridesName = "guicy/jdoConfigOverrides.properties"
InputStream resource = Thread.currentThread().getContextClassLoader().getResourceAsStream(overridesName)
Properties configOverrides = new Properties()
configOverrides.load(resource)
beanFactory.setConfigOverrides(configOverrides)
module.getServletContext().setAttribute(Constants.ATTRIBUTE_BEAN_FACTORY, beanFactory)
module.bind(BeanFactory.class).toInstance(beanFactory)
module.bind(MutableBeanFactory.class).toInstance(beanFactory)
module.bind(JdoBeanFactory.class).toInstance(beanFactory)

log.info "configuring class repository enhancer"
module.bind(ClassRepositoryEnhancer.class).toInstance(new ClassRepositoryEnhancer())

log.info "done."
println "nucleus done."
