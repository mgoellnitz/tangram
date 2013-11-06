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
package org.tangram.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
public class FileRestartCache implements PersistentRestartCache {

    private static final String PERSISTENT_CACHE_FILENAME = "tangram.persistent.cache.ser";

    private static final Log log = LogFactory.getLog(FileRestartCache.class);

    private Map<String, Object> cache = null;


    @SuppressWarnings("unchecked")
    public FileRestartCache() {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(PERSISTENT_CACHE_FILENAME));
            cache = (Map<String, Object>) (ois.readObject());
            ois.close();
        } catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn("() could not load cache starting with an empty set of values");
            } // 
            cache = new HashMap<String, Object>();
        } // try/catch
    } // DummyCacheAdapter()


    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> c) {
        return cache==null ? null : (T) cache.get(key);
    } // get()


    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Type t) {
        return cache==null ? null : (T) cache.get(key);
    } // get()


    @Override
    public <T> void put(String key, T value) {
        if (cache!=null) {
            cache.put(key, value);
            try {
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(PERSISTENT_CACHE_FILENAME));
                oos.writeObject(cache);
                oos.close();
            } catch (Exception e) {
                log.error("put()", e);
            } // try/catch
        } // if
    } // put()

} // FileRestartCache
