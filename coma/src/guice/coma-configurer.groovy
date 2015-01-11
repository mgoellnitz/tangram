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
import org.tangram.PersistentRestartCache
import org.tangram.components.coma.ComaHandler
import org.tangram.view.PropertyConverter
import org.tangram.security.GenericLoginSupport
import org.tangram.security.LoginSupport
import org.tangram.util.DummyRestartCache
import org.tangram.view.GenericPropertyConverter

log.info "starting"

log.info "configuring persistent restart cache"
module.bind(PersistentRestartCache.class).toInstance(new DummyRestartCache())

log.info "configuring property converter"
module.bind(PropertyConverter.class).toInstance(new GenericPropertyConverter())

log.info("configuring login support")
module.bind(LoginSupport.class).toInstance(new GenericLoginSupport())

log.info "configuring coma handler"
module.bind(ComaHandler.class).toInstance(new ComaHandler())

log.info "done."
println "coma done."
