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
import javax.inject.Singleton;
import org.tangram.PersistentRestartCache;

/**
 * Non functional implementation of the persistent restart cache interface.
 *
 * Since Google App Engine provides such a nice persistent cache and we want to generically use this we had to
 * implement the interface for other flavours of tangram as google app engine as well.
 */
@Singleton
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
