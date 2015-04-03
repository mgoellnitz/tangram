/**
 *
 * Copyright 2013-2015 Martin Goellnitz
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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.PersistentRestartCache;


/**
 *
 * Implementation of the persistent restart cache interface using a single file.
 *
 * Since Google App Engine provides such a nice persistent cache and we want to generically use this we had to implement
 * the interface for other flavours of tangram as google app engine as well
 *
 */
public class FileRestartCache implements PersistentRestartCache {

    private static final String PERSISTENT_CACHE_FILENAME_DEFAULT = "tangram.persistent.cache.ser";

    private static final Logger LOG = LoggerFactory.getLogger(FileRestartCache.class);

    private String filename = PERSISTENT_CACHE_FILENAME_DEFAULT;

    private Map<String, Object> cache = null;


    public void setFilename(String filename) {
        this.filename = filename;
    }


    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> c) {
        return cache==null ? null : (T)cache.get(key);
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
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
                oos.writeObject(cache);
            } catch (IOException e) {
                LOG.error("put()", e);
            } // try/catch
        } // if
    } // put()


    @PostConstruct
    public void afterPropertiesSet() {
        LOG.info("afterPropertiesSet({})", filename);
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            cache = SystemUtils.convert(ois.readObject());
        } catch (Exception e) {
            LOG.warn("afterPropertiesSet() could not load cache '{}' starting with an empty set of values", filename);
            cache = new HashMap<>();
        } // try/catch
    } // afterPropertiesSet()

} // FileRestartCache
