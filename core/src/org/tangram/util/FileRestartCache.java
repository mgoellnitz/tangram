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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
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
 * Since Google App Engine provides such a nice persistent cache and we want to generically use this, we had to implement
 * the interface for other flavours of tangram as google app engine as well.
 *
 * Be sure to provide a markerResourceName pointing to a resource/folder in your most frequently changing jar - e.g. the
 * data model jar. Otherwise the cache contents might be out of date and result in strange error conditions. (wipe the
 * file in such cases.)
 *
 */
public class FileRestartCache implements PersistentRestartCache {

    private static final String PERSISTENT_CACHE_FILENAME_DEFAULT = "tangram.persistent.cache.ser";

    private static final String PERSISTENT_CACHE_HASH_KEY = "tangram.cache.hash";

    private static final Logger LOG = LoggerFactory.getLogger(FileRestartCache.class);

    private String filename = PERSISTENT_CACHE_FILENAME_DEFAULT;

    private String markerResourceName = "org/tangram/content";

    private Map<String, Object> cache = null;


    public void setFilename(String filename) {
        this.filename = filename;
    }


    /**
     * Name of a marker resource to generate information, if the file cache is still valid.
     * So this should be a full class file path or folder path contained in your most frequently changed JAR.
     *
     * @param markerResourceName file name with paths and extension for a resource, folder, or class.
     */
    public void setMarkerResourceName(String markerResourceName) {
        this.markerResourceName = markerResourceName;
    }


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
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
                oos.writeObject(cache);
            } catch (IOException e) {
                LOG.error("put()", e);
            } // try/catch
        } // if
    } // put()


    @PostConstruct
    public void afterPropertiesSet() {
        // determine build date
        String hashValue = "";
        try {
            LOG.info("afterPropertiesSet() looking up {}", markerResourceName);
            URL packageUrl = Thread.currentThread().getContextClassLoader().getResource(markerResourceName);
            if (packageUrl!=null) {
                LOG.info("afterPropertiesSet() package url is {}", packageUrl);
                File markerFile = null;
                // Resource found in JAR:
                if (packageUrl.getProtocol().equals("jar")) {
                    URL jarUrl = new URL(packageUrl.getFile());
                    String uriString = jarUrl.toURI().getPath();
                    int idx = uriString.indexOf('!');
                    if (idx>0) {
                        uriString = uriString.substring(0, idx);
                    } // if
                    markerFile = new File(uriString);
                } // if
                // Found in classes directory:
                if (packageUrl.getProtocol().equals("file")) {
                    markerFile = new File(packageUrl.toURI());
                } // if
                LOG.info("afterPropertiesSet() look up returned {}", markerFile);
                SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                hashValue = formatter.format(new Date(markerFile.lastModified()));
            } else {
                LOG.warn("afterPropertiesSet() no resource found for {}", markerResourceName);
            } // if
        } catch (MalformedURLException|URISyntaxException e) {
            LOG.info("afterPropertiesSet() url syntax error which you are not expected to see.", e);
        } // try/catch

        LOG.info("afterPropertiesSet({})", filename);
        boolean resetCache = true;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            cache = SystemUtils.convert(ois.readObject());
            Object storedHash = cache.get(PERSISTENT_CACHE_HASH_KEY);
            LOG.info("afterPropertiesSet() stored {} vs. generated {}", storedHash, hashValue);
            resetCache = ((storedHash==null)||(!(storedHash.toString().equals(hashValue))));
        } catch (Exception e) {
            LOG.warn("afterPropertiesSet() could not load cache '{}' starting with an empty set of values", filename);
        } // try/catch
        if (resetCache) {
            LOG.info("afterPropertiesSet() resetting cache");
            cache = new HashMap<>();
            cache.put(PERSISTENT_CACHE_HASH_KEY, hashValue);
        } // if
    } // afterPropertiesSet()

} // FileRestartCache
