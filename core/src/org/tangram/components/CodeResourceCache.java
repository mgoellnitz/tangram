/**
 * 
 * Copyright 2011-2013 Martin Goellnitz
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
package org.tangram.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.tangram.PersistentRestartCache;
import org.tangram.content.BeanFactory;
import org.tangram.content.BeanListener;
import org.tangram.content.CodeResource;
import org.tangram.content.TransientCode;

@Component
public class CodeResourceCache implements InitializingBean, BeanListener {

    private static final String CODE_RESOURCE_CACHE_KEY = "tangram.code.resource.cache";

    private static Log log = LogFactory.getLog(CodeResourceCache.class);

    @Autowired
    private PersistentRestartCache startupCache;

    @Autowired
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

            // Only use transient resources and cache them
            resources = new ArrayList<CodeResource>();
            for (CodeResource resource : datastoreResources) {
                CodeResource transientResource = new TransientCode(resource);
                resources.add(transientResource);
            } // for
            startupCache.put(CODE_RESOURCE_CACHE_KEY, resources);
        } // if
        resourceCache = new HashMap<String, Map<String, CodeResource>>(resources.size());
        cache = new HashMap<String, CodeResource>(resourceCache.size());
        for (CodeResource resource : resources) {
            cache.put(resource.getId(), resource);
            String mimeType = resource.getMimeType();
            if (StringUtils.hasText(mimeType)) {
                Map<String, CodeResource> typeCache = resourceCache.get(mimeType);
                if (typeCache==null) {
                    typeCache = new HashMap<String, CodeResource>();
                    resourceCache.put(mimeType, typeCache);
                } // if
                if (StringUtils.hasText(resource.getAnnotation())) {
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


    public long getLastUpdate() {
        return lastResetTime;
    } // getLastUpdate()


    public CodeResource get(String mimeType, String annotation) {
        Map<String, CodeResource> typeCache = resourceCache.get(mimeType);
        return (typeCache==null) ? null : typeCache.get(annotation);
    } // get()


    public CodeResource get(String id) {
        return cache.get(id);
    } // get()


    public Collection<String> getAnnotations(String mimeType) {
        Map<String, CodeResource> typeCache = resourceCache.get(mimeType);
        return (typeCache!=null) ? typeCache.keySet() : new HashSet<String>();
    } // getAnnotations()


    public Map<String, CodeResource> getTypeCache(String mimeType) {
        Map<String, CodeResource> typeCache = resourceCache.get(mimeType);
        return (typeCache!=null) ? typeCache : new HashMap<String, CodeResource>();
    } // getTypeCache();


    public void addListener(BeanListener listener) {
        synchronized (attachedListeners) {
            attachedListeners.add(listener);
        } // sync
    } // attachMapToChanges()


    @Override
    public void afterPropertiesSet() throws Exception {
        factory.addListener(CodeResource.class, this);
        reset();
    } // afterPropertiesSet()

} // CodeResourceCache
