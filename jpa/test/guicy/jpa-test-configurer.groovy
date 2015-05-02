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
import org.tangram.PersistentRestartCache
import org.tangram.util.DummyRestartCache
import javax.servlet.ServletContext

log.info "starting"

PersistentRestartCache persistentRestartCache = new DummyRestartCache()
log.info("configuring persistent restart cache {}", persistentRestartCache)
module.bind(PersistentRestartCache.class).toInstance(persistentRestartCache)

log.info("configuring empty name password mapping")
Map<String,String> mapping = new HashMap<>()
module.bind(module.stringStringMap).annotatedWith(Names.named("usernamePasswordMapping")).toInstance(mapping)

log.info "done."
