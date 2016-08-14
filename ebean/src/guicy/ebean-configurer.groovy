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
import org.avaje.datasource.DataSourceConfig
import com.avaje.ebean.config.ServerConfig
import org.tangram.Constants
import org.tangram.util.SystemUtils
import org.tangram.view.PropertyConverter
import org.tangram.content.BeanFactory
import org.tangram.ebean.EBeanFactoryImpl
import org.tangram.mutable.MutableBeanFactory
import org.tangram.view.GenericPropertyConverter
import com.google.inject.TypeLiteral
import java.lang.reflect.Type

log.info "starting"

log.info "configuring property converter"
module.bind(PropertyConverter.class).toInstance(new GenericPropertyConverter())

log.info "configuring bean factory"
DataSourceConfig dataSourceConfig = new DataSourceConfig()
dataSourceConfig.setDriver(config.getProperty("tangram.ebean.driver", "org.h2.Driver"))
dataSourceConfig.setUrl(config.getProperty("tangram.ebean.url", "jdbc:h2:./h2/tangram"))
dataSourceConfig.setUsername(config.getProperty("tangram.ebean.username", "sa"))
dataSourceConfig.setPassword(config.getProperty("tangram.ebean.password", ""))
dataSourceConfig.setHeartbeatSql(config.getProperty("tangram.ebean.hearbeat.sql", "select 1"))
dataSourceConfig.setMaxConnections(Integer.parseInt(config.getProperty("tangram.ebean.max.connections", "5")))

ServerConfig serverConfig = new ServerConfig()
serverConfig.setDataSourceConfig(dataSourceConfig)
serverConfig.setName("tangram")
serverConfig.setDefaultServer(false)
serverConfig.setRegister(false)
serverConfig.setDdlGenerate(config.getProperty("tangram.ebean.ddl.generate", "false").equals("true"))
serverConfig.setDdlRun(config.getProperty("tangram.ebean.ddl.run", "false").equals("true"))

Set<String> basePackages = SystemUtils.stringSetFromParameterString(config.getProperty("basePackages", "org.tangram"))
EBeanFactoryImpl beanFactory = new EBeanFactoryImpl()
beanFactory.setBasePackages(basePackages)
beanFactory.setServerConfig(serverConfig)
module.getServletContext().setAttribute(Constants.ATTRIBUTE_BEAN_FACTORY, beanFactory)
module.bind(BeanFactory.class).toInstance(beanFactory)
Object vehicle = new Object() {
  MutableBeanFactory<?, ?> v
};
Type interimType = vehicle.getClass().getDeclaredField("v").getGenericType()
TypeLiteral mbf = TypeLiteral.get(interimType)
module.bind(mbf).toInstance(beanFactory)
module.bind(MutableBeanFactory.class).toInstance(beanFactory)

log.info "done."
println "ebean done."
