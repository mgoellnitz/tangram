/**
 * 
 * Copyright 2013 Martin Goellnitz
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package org.tangram.util;

import java.lang.reflect.Type;

import org.springframework.stereotype.Component;
import org.tangram.PersistentRestartCache;

/**
 * 
 * Since Google App Engine provides such a nice persistent cache and we want to generically use this we had to implement
 * the interface for other flavours of tangram as google app engine as well
 * 
 * The file cache adapter moved to the core package because it was not really rdbms or mongo dependent and can also be 
 * used out of the jdo scope. This reasulted in the fact that using modules have to declare it in XML files and cannot
 * be auto-scanned anymore.
 * 
 * @author Martin Goellnitz
 * 
 */
@Component
public class DummyRestartCache implements PersistentRestartCache {

    public DummyRestartCache() {
    } // DummyCacheAdapter()


    @Override
    public <T> T get(String key, Class<T> c) {
        return null;
    } // get()


    @Override
    public <T> T get(String key, Type t) {
        return null;
    } // get()


    @Override
    public <T> void put(String key, T value) {
    } // put()

} // DummyRestartCache
