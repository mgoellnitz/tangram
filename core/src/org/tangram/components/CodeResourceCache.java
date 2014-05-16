/**
 *
 * Copyright 2013-2014 Martin Goellnitz
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
package org.tangram.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tangram.PersistentRestartCache;
import org.tangram.content.BeanFactory;
import org.tangram.content.BeanListener;
import org.tangram.content.CodeResource;
import org.tangram.content.TransientCode;


/**
 * Cache for code resources taken from the repository represented by the bean factory.
 *
 * The cache is filled with transient implementations of the code resource interfaces resembling contents taken
 * from the bean factory by query. The persistent restart cache is used to support much faster restart since
 * the values in this cache are not supposed to change betweetn restarts.
 *
 */
@Named
@Singleton
public class CodeResourceCache implements BeanListener {

    private static final String CODE_RESOURCE_CACHE_KEY = "tangram.code.resource.cache";

    private static final Log log = LogFactory.getLog(CodeResourceCache.class);

    @Inject
    private PersistentRestartCache startupCache;

    @Inject
    private BeanFactory factory;

    private long lastResetTime;

    private List<BeanListener> attachedListeners = new ArrayList<BeanListener>();

    private Map<String, Map<String, CodeResource>> resourceCache;

    private Map<String, CodeResource> cache;


    @Override
    @SuppressWarnings("unchecked")
    public void reset() {
        List<CodeResource> resources = null;
        if (resourceCache==null) {
            // just started
            resources = startupCache.get(CODE_RESOURCE_CACHE_KEY, List.class);
            if (log.isInfoEnabled()) {
                log.info("reset() cache: "+resources);
            } // if
        } // if
        if (resources==null) {
            if (log.isInfoEnabled()) {
                log.info("reset() obtaining all code resources");
            } // if
            List<CodeResource> datastoreResources = factory.listBeans(CodeResource.class);

            // Only use transient resources and cache them since we can't put persistable stuff in the startup cache
            resources = new ArrayList<CodeResource>();
            for (CodeResource resource : datastoreResources) {
                resources.add(new TransientCode(resource));
            } // for
            startupCache.put(CODE_RESOURCE_CACHE_KEY, resources);
        } // if
        resourceCache = new HashMap<String, Map<String, CodeResource>>(resources.size());
        cache = new HashMap<String, CodeResource>(resourceCache.size());
        for (CodeResource resource : resources) {
            cache.put(resource.getId(), resource);
            String mimeType = resource.getMimeType();

            if (StringUtils.isNotBlank(mimeType)) {
                Map<String, CodeResource> typeCache = resourceCache.get(mimeType);
                if (typeCache==null) {
                    typeCache = new HashMap<String, CodeResource>();
                    resourceCache.put(mimeType, typeCache);
                } // if
                if (StringUtils.isNotEmpty(resource.getAnnotation())) {
                    typeCache.put(resource.getAnnotation(), resource);
                } // if
            } // if
        } // for
        if (log.isInfoEnabled()) {
            log.info("reset() code resources obtained");
        } // if
        for (BeanListener listener : attachedListeners) {
            listener.reset();
        } // for
        if (log.isInfoEnabled()) {
            log.info("reset() listeners notified");
        } // if
        lastResetTime = System.currentTimeMillis();
    } // reset()


    /**
     * return a pseudo file modification time.
     *
     * It takes the time of last code resource scan to be used by e.g. the velocity parser again for caching purposes
     *
     * @return last time of scanning for code resources
     */
    public long getLastUpdate() {
        return lastResetTime;
    } // getLastUpdate()


    /**
     * Obtain the code resource for a given mimetype and annotation.
     *
     * @param mimeType mimetype for the cache part to search in
     * @param annotation annotation of the code resource to be returned
     * @return a code resource if found or null otherwise
     */
    public CodeResource get(String mimeType, String annotation) {
        Map<String, CodeResource> typeCache = resourceCache.get(mimeType);
        return (typeCache==null) ? null : typeCache.get(annotation);
    } // get()


    /**
     * Get a code resource for a id from the code resource cache.
     *
     * Quite similar to a getBean() in the bean factory
     *
     * @param id
     * @return code resource for the id if found or null otherwise
     */
    public CodeResource get(String id) {
        return cache.get(id);
    } // get()


    /**
     * Return a list of available mimetype in the cache.
     *
     * @return set of mimetypes - may be empty but not null
     */
    public Set<String> getTypes() {
        return resourceCache.keySet();
    } // getTypes()


    /**
     * Return all codes in the cache. unordered.
     *
     * @return collection of code resources - may be empty but not null
     */
    public Collection<CodeResource> getCodes() {
        return cache.values();
    } // getCodes()


    /**
     * Get all available annotations for a given mimetype.
     *
     * Since thois anotations are used like filenames this is something like a listing for the given mimetype
     *
     * @param mimeType
     * @return list of annotations for the given mimetype - maybe empty but nut null
     */
    public Collection<String> getAnnotations(String mimeType) {
        Map<String, CodeResource> typeCache = resourceCache.get(mimeType);
        return (typeCache!=null) ? typeCache.keySet() : new HashSet<String>();
    } // getAnnotations()


    /**
     * Return the cache portion for a given mimetype.
     *
     * @param mimeType
     * @return annotation to code resource map for the given mimetype - maybe empty but not null
     */
    public Map<String, CodeResource> getTypeCache(String mimeType) {
        Map<String, CodeResource> typeCache = resourceCache.get(mimeType);
        return (typeCache!=null) ? typeCache : new HashMap<String, CodeResource>();
    } // getTypeCache();


    public void addListener(BeanListener listener) {
        synchronized (attachedListeners) {
            attachedListeners.add(listener);
        } // sync
    } // attachListener()


    @PostConstruct
    public void afterPropertiesSet() throws Exception {
        factory.addListener(CodeResource.class, this);
        reset();
    } // afterPropertiesSet()

} // CodeResourceCache
