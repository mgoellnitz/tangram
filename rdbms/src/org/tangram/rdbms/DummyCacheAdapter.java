/**
 * 
 * Copyright 2013 Martin Goellnitz
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package org.tangram.rdbms;

import org.springframework.stereotype.Component;
import org.tangram.PersistentRestartCache;

@Component
public class DummyCacheAdapter implements PersistentRestartCache {

    @Override
    public <T> T get(String key, Class<T> c) {
        return null;
    }


    @Override
    public <T> void put(String key, T value) {
    }

} // DummyCacheAdapter
