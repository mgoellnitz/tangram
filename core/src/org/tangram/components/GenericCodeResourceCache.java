/**
 *
 * Copyright 2013-2020 Martin Goellnitz
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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.PersistentRestartCache;
import org.tangram.content.BeanFactory;
import org.tangram.content.BeanListener;
import org.tangram.content.CodeResource;
import org.tangram.content.CodeResourceCache;
import org.tangram.content.TransientCode;
import org.tangram.util.SystemUtils;


/**
 * Cache for code resources taken from the repository represented by the bean factory.
 *
 * The cache is filled with transient implementations of the code resource interfaces resembling contents taken
 * from the bean factory by query. The persistent restart cache is used to support much faster restart since
 * the values in this cache are not supposed to change between restarts.
 *
 */
@Named("codeResourceCache")
@Singleton
public class GenericCodeResourceCache implements CodeResourceCache {

    private static final String CODE_RESOURCE_CACHE_KEY = "tangram.code.resource.cache";

    private static final Logger LOG = LoggerFactory.getLogger(GenericCodeResourceCache.class);

    @Inject
    private PersistentRestartCache startupCache;

    @Inject
    private BeanFactory<?> factory;

    private long lastResetTime;

    private final List<BeanListener> attachedListeners = new ArrayList<>();

    private Map<String, Map<String, CodeResource>> resourceCache;

    private Map<String, CodeResource> cache;


    /**
     * Resets all cache content and reads inderlying resource anew.
     */
    @Override
    public void reset() {
        List<CodeResource> resources = null;
        if (resourceCache==null) {
            // just started
            resources = SystemUtils.convert(startupCache.get(CODE_RESOURCE_CACHE_KEY, List.class));
            LOG.info("reset() cache: {}", resources);
        } // if
        if (resources==null) {
            LOG.info("reset() obtaining all code resources");
            List<CodeResource> datastoreResources = factory.listBeans(CodeResource.class);

            // Only use transient resources and cache them since we can't put persistable stuff in the startup cache
            resources = new ArrayList<>();
            for (CodeResource resource : datastoreResources) {
                resources.add(new TransientCode(resource));
            } // for
            startupCache.put(CODE_RESOURCE_CACHE_KEY, resources);
        } // if
        resourceCache = new HashMap<>(resources.size());
        cache = new HashMap<>(resourceCache.size());
        for (CodeResource resource : resources) {
            cache.put(resource.getId(), resource);
            String mimeType = resource.getMimeType();

            if (StringUtils.isNotBlank(mimeType)) {
                Map<String, CodeResource> typeCache = resourceCache.get(mimeType);
                if (typeCache==null) {
                    typeCache = new HashMap<>();
                    resourceCache.put(mimeType, typeCache);
                } // if
                if (StringUtils.isNotEmpty(resource.getAnnotation())) {
                    typeCache.put(resource.getAnnotation(), resource);
                } // if
            } // if
        } // for
        LOG.info("reset() code resources obtained");
        for (BeanListener listener : attachedListeners) {
            listener.reset();
        } // for
        LOG.info("reset() listeners notified");
        lastResetTime = System.currentTimeMillis();
    } // reset()


    /**
     * return a pseudo file modification time.
     *
     * It takes the time of last code resource scan to be used by e.g. the velocity parser again for caching purposes
     *
     * @return last time of scanning for code resources
     */
    @Override
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
    @Override
    public CodeResource get(String mimeType, String annotation) {
        Map<String, CodeResource> typeCache = resourceCache.get(mimeType);
        return (typeCache==null) ? null : typeCache.get(annotation);
    } // get()


    /**
     * Get a code resource for an id from the code resource cache.
     *
     * Quite similar to a getBean() in the bean factory
     *
     * @param id content id of the code resource instance to obtain
     * @return code resource for the id if found or null otherwise
     */
    @Override
    public CodeResource get(String id) {
        return cache.get(id);
    } // get()


    /**
     * Return a list of available mimetype in the cache.
     *
     * @return set of mimetypes - may be empty but not null
     */
    @Override
    public Set<String> getTypes() {
        return resourceCache.keySet();
    } // getTypes()


    /**
     * Return all codes in the cache. unordered.
     *
     * @return collection of code resources - may be empty but not null
     */
    @Override
    public Collection<CodeResource> getCodes() {
        return cache.values();
    } // getCodes()


    /**
     * Get all available annotations for a given mimetype.
     *
     * Since thois anotations are used like filenames this is something like a listing for the given mimetype
     *
     * @param mimeType mimetype of the code resource instances to collect annotation string from
     * @return list of annotations for the given mimetype - maybe empty but nut null
     */
    @Override
    public Collection<String> getAnnotations(String mimeType) {
        Map<String, CodeResource> typeCache = resourceCache.get(mimeType);
        return (typeCache!=null) ? typeCache.keySet() : new HashSet<String>();
    } // getAnnotations()


    /**
     * Return the cache portion for a given mimetype.
     *
     * @param mimeType mime type of the code resources to include into result mapping
     * @return annotation to code resource map for the given mimetype - maybe empty but not null
     */
    @Override
    public Map<String, CodeResource> getTypeCache(String mimeType) {
        Map<String, CodeResource> typeCache = resourceCache.get(mimeType);
        return (typeCache!=null) ? typeCache : new HashMap<>();
    } // getTypeCache();


    @Override
    public void addListener(BeanListener listener) {
        synchronized (attachedListeners) {
            attachedListeners.add(listener);
        } // synchronized
        listener.reset();
    } // addListener()


    @PostConstruct
    public void afterPropertiesSet() {
        LOG.debug("afterPropertiesSet()");
        factory.addListener(CodeResource.class, this);
    } // afterPropertiesSet()

} // GenericCodeResourceCache
