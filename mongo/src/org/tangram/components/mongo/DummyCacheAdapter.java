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
package org.tangram.components.mongo;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.tangram.PersistentRestartCache;

/**
 * 
 * Since Google App Engine provides such a nice persistent cache and we want to generically use this we had to implement
 * the interface for other flavours of tangram as google app engine as well
 * 
 * this implementation here is not that dummy anymore as the name indicates
 * 
 * @author Martin Goellnitz
 * 
 */
@Component
public class DummyCacheAdapter implements PersistentRestartCache {

    private static final String PERSISTENT_CACHE_FILENAME = "tangram.persistent.cache.ser";

    private static final Log log = LogFactory.getLog(DummyCacheAdapter.class);

    private Map<String, Object> jsrCache = null;


    @SuppressWarnings("unchecked")
    public DummyCacheAdapter() {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(PERSISTENT_CACHE_FILENAME));
            jsrCache = (Map<String, Object>)(ois.readObject());
            ois.close();
        } catch (Exception e) {
            jsrCache = new HashMap<String, Object>();
        } // try/catch
    } // DummyCacheAdapter()


    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> c) {
        return jsrCache==null ? null : (T)jsrCache.get(key);
    } // get()


    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Type t) {
        return jsrCache==null ? null : (T)jsrCache.get(key);
    } // get()


    @Override
    public <T> void put(String key, T value) {
        if (jsrCache!=null) {
            jsrCache.put(key, value);
            try {
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(PERSISTENT_CACHE_FILENAME));
                oos.writeObject(jsrCache);
                oos.close();
            } catch (Exception e) {
                log.error("put()", e);
            } // try/catch
        } // if
    } // put()

} // DummyCacheAdapter
