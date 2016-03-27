/**
 *
 * Copyright 2016 Martin Goellnitz
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
import org.tangram.PersistentRestartCache
import org.tangram.content.BeanFactory
import org.tangram.mock.content.MockBeanFactory
import org.tangram.util.DummyRestartCache
import org.tangram.view.GenericPropertyConverter
import org.tangram.view.PropertyConverter

log.info "starting"

log.info "configuring bean facory"
BeanFactory beanFactory = new MockBeanFactory()
beanFactory.init()
module.bind(BeanFactory.class).toInstance(beanFactory)

log.info "configuring property converter"
PropertyConverter propertyConverter = new GenericPropertyConverter()
module.bind(PropertyConverter.class).toInstance(propertyConverter)

log.info "configuring persistent restart cache"
PersistentRestartCache persistentRestartCache = new DummyRestartCache()
module.bind(PersistentRestartCache.class).toInstance(persistentRestartCache)

log.info("configuring name password mapping")
Map<String,String> mapping = new HashMap<>()
mapping.put('admin', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918')
mapping.put('user', '04f8996da763b7a969b1028ee3007569eaf3a635486ddab211d512c85b9df8fb')
module.bind(module.stringStringMap).annotatedWith(Names.named("usernamePasswordMapping")).toInstance(mapping)

log.info "done."
println "nucleus done."
