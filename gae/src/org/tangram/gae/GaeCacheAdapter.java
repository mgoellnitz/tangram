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
package org.tangram.gae;

import java.util.Collections;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheFactory;
import net.sf.jsr107cache.CacheManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.tangram.PersistentRestartCache;

@Component
public class GaeCacheAdapter implements PersistentRestartCache {

    private static final Log log = LogFactory.getLog(GaeBeanFactory.class);

    private Cache jsrCache;


    public GaeCacheAdapter() {
        try {
            CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
            jsrCache = cacheFactory.createCache(Collections.emptyMap());
        } catch (CacheException ce) {
            log.error("()", ce);
        } // try
    } // GaeCacheAdapter()


    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> c) {
        return jsrCache==null ? null : (T)jsrCache.get(key);
    } // get()


    @Override
    public <T> void put(String key, T value) {
        if (jsrCache!=null) {
            jsrCache.put(key, value);
        } // if
    } // Put()

} // GaeCacheAdapter
