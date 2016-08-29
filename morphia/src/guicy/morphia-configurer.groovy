/**
 *
 * Copyright 2015-2016 Martin Goellnitz
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
import org.tangram.util.SystemUtils
import org.tangram.view.PropertyConverter
import org.tangram.content.BeanFactory
import org.tangram.morphia.MorphiaBeanFactory
import org.tangram.mutable.MutableBeanFactory
import org.tangram.view.GenericPropertyConverter
import com.google.inject.TypeLiteral
import java.lang.reflect.Type

log.info "starting"

log.info "configuring property converter"
module.bind(PropertyConverter.class).toInstance(new GenericPropertyConverter())

log.info "configuring bean factory"
Set<String> basePackages = SystemUtils.stringSetFromParameterString(config.getProperty("basePackages", "org.tangram"))
MorphiaBeanFactory beanFactory = new MorphiaBeanFactory()
beanFactory.setUri(config.getProperty("mongo.uri", "mongodb://localhost:27017/"))
beanFactory.setDatabase(config.getProperty("mongo.database", "tangram"))
beanFactory.setBasePackages(basePackages)
module.getServletContext().setAttribute(Constants.ATTRIBUTE_BEAN_FACTORY, beanFactory)
Object vehicle = new Object() {
  BeanFactory<?> v
};
Type interimType = vehicle.getClass().getDeclaredField("v").getGenericType()
TypeLiteral bf = TypeLiteral.get(interimType)
module.bind(bf).toInstance(beanFactory)
module.bind(BeanFactory.class).toInstance(beanFactory)
Object secondVehicle = new Object() {
  MutableBeanFactory<?, ?> v
};
interimType = secondVehicle.getClass().getDeclaredField("v").getGenericType()
TypeLiteral mbf = TypeLiteral.get(interimType)
module.bind(mbf).toInstance(beanFactory)
module.bind(MutableBeanFactory.class).toInstance(beanFactory)

log.info "done."
println "morphia done."
