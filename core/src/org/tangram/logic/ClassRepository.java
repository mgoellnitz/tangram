/**
 * 
 * Copyright 2011 Martin Goellnitz
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
package org.tangram.logic;

import groovy.lang.GroovyClassLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.control.CompilationFailedException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tangram.Constants;
import org.tangram.content.BeanListener;
import org.tangram.content.CodeResource;
import org.tangram.content.CodeResourceCache;

@Component
public class ClassRepository implements InitializingBean, BeanListener {

    @Autowired
    private CodeResourceCache codeCache;

    private Map<String, Class<? extends Object>> classes;

    private Map<String, String> compilationErrors;

    private List<BeanListener> attachedListeners = new ArrayList<BeanListener>();

    private static Log log = LogFactory.getLog(ClassRepository.class);


    @SuppressWarnings("unchecked")
    private void fillClasses() {
        compilationErrors = new HashMap<String,String>();
        classes = new HashMap<String, Class<? extends Object>>();

        Map<String, String> codes = new HashMap<String, String>();

        GroovyClassLoader gcl = new GroovyClassLoader();
        Map<String, CodeResource> typeCache = codeCache.getTypeCache("application/x-groovy");
        for (CodeResource resource : typeCache.values()) {
            String annotation = resource.getAnnotation();
            // Check for class name - must be with capital letter for last element
            int idx = annotation.lastIndexOf('.');
            if (log.isInfoEnabled()) {
                log.info("fillClasses() checking for class name "+annotation+" ("+idx+")");
            } // if
            if (idx>=0) {
                idx++ ;
                String suffix = annotation.substring(idx);
                if ( !Character.isLowerCase(suffix.charAt(0))) {
                    try {
                        String codeText = resource.getCodeText();
                        codes.put(annotation, codeText);
                    } catch (Throwable e) {
                        // who cares
                        if (log.isErrorEnabled()) {
                            log.error("fillClasses()", e);
                        } // if
                    } // try/catch
                } // if
            } // if
        } // for
        try {
            gcl.close();
        } catch (Exception e) {
            log.error("could not close groovy class loader", e);
        } // try/catch

        int i = Constants.RIP_CORD_COUNT;
        while (i-- >0&&codes.size()>classes.size()) {
            for (Map.Entry<String, String> code : codes.entrySet()) {
                try {
                    if (log.isInfoEnabled()) {
                        log.info("fillClasses() compiling "+code.getKey());
                    } // if
                    Class<? extends Object> clazz = gcl.parseClass(code.getValue(), code.getKey()+".groovy");
                    classes.put(code.getKey(), clazz);
                } catch (CompilationFailedException cfe) {
                    compilationErrors.put(code.getKey(), cfe.getMessage());
                    log.error("fillClasses()", cfe);
                } catch (Throwable t) {
                    log.error("fillClasses() [not marked in source code]", t);
                } // try/catch
            } // for
        } // while
    }// fillClasses()


    public Set<String> get() {
        return classes.keySet();
    } // get()


    @SuppressWarnings("unchecked")
    public <T extends Object> Map<String, Class<T>> get(Class<? extends T> cls) {
        Map<String, Class<T>> result = new HashMap<String, Class<T>>();
        for (Map.Entry<String, Class<? extends Object>> entry : classes.entrySet()) {
            if (cls.isAssignableFrom(entry.getValue())) {
                result.put(entry.getKey(), (Class<T>)entry.getValue());
            } // if
        } // for
        return result;
    } // get()


    public Class<? extends Object> get(String annotation) {
        return classes.get(annotation);
    } // get()


    public Map<String, String> getCompilationErrors() {
        return compilationErrors;
    }


    public void addListener(BeanListener listener) {
        synchronized (attachedListeners) {
            attachedListeners.add(listener);
        } // sync
    } // attachMapToChanges()


    @Override
    public void reset() {
        fillClasses();
        for (BeanListener listener : attachedListeners) {
            listener.reset();
        } // for
    } // reset();


    @Override
    public void afterPropertiesSet() throws Exception {
        codeCache.addListener(this);
        reset();
    } // afterPropertiesSet()

} // ClassRepository
