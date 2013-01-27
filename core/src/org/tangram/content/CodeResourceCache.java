/**
 * 
 * Copyright 2011-2013 Martin Goellnitz
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
package org.tangram.content;

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

@Component
public class CodeResourceCache implements InitializingBean, BeanListener {

    private Map<String, Map<String, CodeResource>> resourceCache;

    @Autowired
    private BeanFactory factory;

    private List<BeanListener> attachedListeners = new ArrayList<BeanListener>();

    private static Log log = LogFactory.getLog(CodeResourceCache.class);


    @Override
    public void reset() {
        resourceCache = new HashMap<String, Map<String, CodeResource>>();
        if (log.isInfoEnabled()) {
            log.info("reset() obtaining all code resources");
        } // if
        List<CodeResource> resources = factory.listBeans(CodeResource.class);
        for (CodeResource resource : resources) {
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

        for (BeanListener listener : attachedListeners) {
            listener.reset();
        } // for
        if (log.isInfoEnabled()) {
            log.info("reset() code resources obtained");
        } // if
    } // reset()


    public CodeResource get(String mimeType, String annotation) {
        Map<String, CodeResource> typeCache = resourceCache.get(mimeType);
        return (typeCache==null) ? null : typeCache.get(annotation);
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
