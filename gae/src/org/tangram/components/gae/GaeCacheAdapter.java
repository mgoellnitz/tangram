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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.tangram.components.gae;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;
import javax.inject.Named;
import javax.inject.Singleton;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheFactory;
import net.sf.jsr107cache.CacheManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tangram.PersistentRestartCache;
import org.tangram.gae.GaeBeanFactory;


@Named
@Singleton
public class GaeCacheAdapter implements PersistentRestartCache {

    private static final Log log = LogFactory.getLog(GaeBeanFactory.class);

    private Map<String, Object> jsrCache = null;


    @SuppressWarnings("unchecked")
    public GaeCacheAdapter() {
        try {
            CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
            jsrCache = cacheFactory.createCache(Collections.emptyMap());
            if (log.isInfoEnabled()) {
                log.info("() jsrCache="+jsrCache);
            } // if
        } catch (CacheException ce) {
            log.error("()", ce);
        } // try
    }// GaeCacheAdapter()


    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> c) {
        if (log.isDebugEnabled()) {
            log.debug("get() jsrCache="+jsrCache+" key="+key+": "+jsrCache.containsKey(key)+" "+jsrCache.get(key));
        } // if
        return jsrCache==null ? null : (T) jsrCache.get(key);
    } // get()


    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Type t) {
        if (log.isDebugEnabled()) {
            log.debug("get() jsrCache="+jsrCache+" key="+key+": "+jsrCache.containsKey(key)+" "+jsrCache.get(key));
        } // if
        return jsrCache==null ? null : (T) jsrCache.get(key);
    } // get()


    @Override
    public <T> void put(String key, T value) {
        if (jsrCache!=null) {
            jsrCache.put(key, value);
            if (log.isDebugEnabled()) {
                log.debug("put() "+jsrCache.get(key));
            } // if
        } // if
    } // Put()

} // GaeCacheAdapter
