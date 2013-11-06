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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.tangram.components.spring;

import groovy.lang.GroovyClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.tools.GroovyClass;
import org.springframework.beans.factory.InitializingBean;
import org.tangram.Constants;
import org.tangram.PersistentRestartCache;
import org.tangram.content.BeanListener;
import org.tangram.content.CodeResource;
import org.tangram.logic.ClassRepository;


@Named
public class GroovyClassRepository implements ClassRepository, InitializingBean, BeanListener {

    private static final String BYTECODE_CACHE_KEY = "tangram.bytecode.cache";

    private static Log log = LogFactory.getLog(GroovyClassRepository.class);

    @Inject
    private CodeResourceCache codeCache;

    @Inject
    private PersistentRestartCache startupCache;

    private Map<String, Class<? extends Object>> classes = null;

    private Map<String, byte[]> byteCodes = null;

    private Map<String, String> compilationErrors = new HashMap<String, String>();

    private List<BeanListener> attachedListeners = new ArrayList<BeanListener>();

    private GroovyClassLoader classLoader;


    @SuppressWarnings("unchecked")
    protected void fillClasses() {
        byteCodes = startupCache.get(BYTECODE_CACHE_KEY, Map.class);
        if (classes!=null) {
            byteCodes = null;
        } // if
        classLoader = new GroovyClassLoader();
        classes = new HashMap<String, Class<? extends Object>>();
        if (byteCodes==null) {
            compilationErrors = new HashMap<String, String>();
            byteCodes = new HashMap<String, byte[]>();

            Map<String, String> codes = new HashMap<String, String>();
            Map<String, CodeResource> typeCache = codeCache.getTypeCache("application/x-groovy");
            for (CodeResource resource : typeCache.values()) {
                String annotation = resource.getAnnotation();
                // Check for class name - must be with capital letter for last element
                int idx = annotation.lastIndexOf('.');
                if (log.isInfoEnabled()) {
                    log.info("fillClasses() checking for class name "+annotation+" ("+idx+")");
                } // if
                if (idx>=0) {
                    idx++;
                    String suffix = annotation.substring(idx);
                    if (!Character.isLowerCase(suffix.charAt(0))) {
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

            int i = Constants.RIP_CORD_COUNT;
            while (i-->0&&codes.size()>byteCodes.size()) {
                for (Map.Entry<String, String> code : codes.entrySet()) {
                    try {
                        if (log.isInfoEnabled()) {
                            log.info("fillClasses() compiling "+code.getKey());
                        } // if
                        CompilationUnit cu = new CompilationUnit(classLoader);
                        cu.addSource(code.getKey()+".groovy", code.getValue());
                        cu.compile(Phases.CLASS_GENERATION);
                        List<GroovyClass> classList = cu.getClasses();
                        if (classList.size()==1) {
                            GroovyClass groovyClass = classList.get(0);
                            byteCodes.put(groovyClass.getName(), groovyClass.getBytes());
                            Class<? extends Object> clazz = classLoader.defineClass(groovyClass.getName(), groovyClass.getBytes());
                            if (log.isInfoEnabled()) {
                                log.info("fillClasses() defining "+clazz.getName());
                            } // if
                            classes.put(clazz.getName(), clazz);
                        } // if
                    } catch (CompilationFailedException cfe) {
                        compilationErrors.put(code.getKey(), cfe.getMessage());
                        log.error("fillClasses()", cfe);
                    } catch (Throwable t) {
                        log.error("fillClasses() [not marked in source code]", t);
                    } // try/catch
                } // for
            } // while
            startupCache.put(BYTECODE_CACHE_KEY, byteCodes);
        } else {
            for (Map.Entry<String, byte[]> byteCode : byteCodes.entrySet()) {
                if (log.isDebugEnabled()) {
                    log.debug("fillClasses() defining "+byteCode.getKey());
                } // if
                Class<? extends Object> clazz = classLoader.defineClass(byteCode.getKey(), byteCode.getValue());
                classes.put(clazz.getName(), clazz);
            } // if
        } // if
        if (log.isInfoEnabled()) {
            log.info("fillClasses() done");
        } // if
    } // fillClasses()


    public ClassLoader getClassLoader() {
        return classLoader;
    } // getClassLoader()



    public Set<String> get() {
        return classes.keySet();
    } // get()


    @SuppressWarnings("unchecked")
    public <T extends Object> Map<String, Class<T>> get(Class<? extends T> cls) {
        Map<String, Class<T>> result = new HashMap<String, Class<T>>();
        for (Map.Entry<String, Class<? extends Object>> entry : classes.entrySet()) {
            if (cls.isAssignableFrom(entry.getValue())) {
                result.put(entry.getKey(), (Class<T>) entry.getValue());
            } // if
        } // for
        return result;
    } // get()


    public Class<? extends Object> get(String className) {
        return classes.get(className);
    } // get()


    public byte[] getBytes(String className) {
        return byteCodes.get(className);
    } // getBytes()


    /**
     * overriding one class means in this case defining all classes anew with one changed definition.
     *
     * @param className name of the class
     * @param bytes redefined byte code for the class
     */
    @Override
    public void overrideClass(String className, byte[] bytes) {
        if (get(className)!=null) {
            if (log.isInfoEnabled()) {
                log.info("overrideClass() overriding "+className);
            } // if
            byteCodes.put(className, bytes);
            classLoader = new GroovyClassLoader();
            for (String name : byteCodes.keySet()) {
                if (log.isDebugEnabled()) {
                    log.debug("overrideClass() re-defining "+name);
                } // if
                @SuppressWarnings("unchecked")
                Class<? extends Object> clazz = classLoader.defineClass(name, byteCodes.get(name));
                if (log.isDebugEnabled()) {
                    log.debug("overrideClass() re-defining "+clazz.getName());
                } // if
                classes.put(clazz.getName(), clazz);
            } // for
        } // if
    } // overrideClass()


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

} // GroovyClassRepository
