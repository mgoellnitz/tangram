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
package org.tangram.mutable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tangram.PersistentRestartCache;
import org.tangram.content.AbstractBeanFactory;
import org.tangram.content.BeanListener;
import org.tangram.content.Content;
import org.tangram.monitor.Statistics;


/**
 * Common stuff for all bean factories dealing with mutable content.
 */
public abstract class AbstractMutableBeanFactory extends AbstractBeanFactory implements MutableBeanFactory {

    private static final Log log = LogFactory.getLog(AbstractMutableBeanFactory.class);

    protected static final String QUERY_CACHE_KEY = "tangram.query.cache";

    @Inject
    protected Statistics statistics;

    @Inject
    protected PersistentRestartCache startupCache;

    private Map<Class<? extends Content>, List<BeanListener>> attachedListeners = new HashMap<Class<? extends Content>, List<BeanListener>>();

    /**
     * mapping from classes or interfaces to non abstract classes implementing them
     */
    protected Map<Class<? extends Content>, List<Class<? extends Content>>> implementingClassesMap = null;

    protected Map<String, Content> cache = new HashMap<String, Content>();

    private boolean activateCaching = false;


    protected Map<Class<? extends Content>, List<BeanListener>> getListeners() {
        return attachedListeners;
    } // getListeners() {


    /**
     * Check if the underlying API implementation of the bean factory really has the needed managing instance
     * of some sort at hand.
     *
     * common case to check for persisting and deleting instanced though highly API specific in detail.
     */
    protected abstract boolean hasManager();


    public boolean isActivateCaching() {
        return activateCaching;
    }


    public void setActivateCaching(boolean activateCaching) {
        this.activateCaching = activateCaching;
    }


    /**
     * Wrap API specific persistence call.
     * Higher level methods in this class in turn deal with exception and cachce handling.
     *
     * @param <T>
     * @param bean
     */
    protected abstract <T extends Content> void apiPersist(T bean);


    /**
     * Wrap API specific deletion call.
     * Higher level methods in this class in turn deal with exception and cachce handling.
     *
     * @param <T>
     * @param bean
     */
    protected abstract <T extends Content> void apiDelete(T bean);


    /**
     * Cache key for the persistent cache to store all class names.
     * The stored values are taken from the class path package scan and
     * asumed to be persistent over re-starts of the applicaion.
     *
     * @return String to be used as a cache key
     */
    protected String getClassNamesCacheKey() {
        return "tangram-class-names";
    } // getClassNamesCacheKey()


    @Override
    public <T extends Content> boolean persistUncommitted(T bean) {
        boolean result = false;
        boolean rollback = true;
        try {
            apiPersist(bean);
            rollback = false;
            clearCacheFor(bean.getClass());
            result = true;
        } catch (Exception e) {
            log.error("persistUncommitted()", e);
            if (rollback&&hasManager()) {
                // yes we saw situations where this was not the case thus hiding other errors!
                rollbackTransaction();
            } // if
        } // try/catch/finally
        return result;
    } // persistUncommitted()


    @Override
    public <T extends Content> boolean delete(T bean) {
        boolean result = false;
        boolean rollback = true;
        try {
            apiDelete(bean);
            commitTransaction();
            rollback = false;
            clearCacheFor(bean.getClass());
            result = true;
        } catch (Exception e) {
            log.error("delete()", e);
            if (rollback&&hasManager()) {
                // yes we saw situations where this was not the case thus hiding other errors!
                rollbackTransaction();
            } // if
        } // try/catch/finally
        return result;
    } // delete()


    @Override
    public <T extends Content> boolean persist(T bean) {
        final boolean result = persistUncommitted(bean);
        if (result) {
            commitTransaction();
        } // if
        return result;
    } // persist()


    /**
     * Gets the class for a given type name.
     * Be aware of different class loaders - like with groovy based classes.
     *
     * @param className fully qualified name of the class
     */
    @SuppressWarnings("unchecked")
    protected Class<? extends Content> getClassForName(String className) {
        Class<? extends Content> result = null;
        for (Class<? extends Content> c : getClasses()) {
            if (c.getName().equals(className)) {
                result = c;
            } // if
        } // for
        if (result==null) {
            try {
                result = (Class<? extends Content>) Class.forName(className);
            } catch (ClassNotFoundException cnfe) {
                //
            } // try/catch
        } // if
        return result;
    } // getClassForName()


    /**
     * Get class name from query cache key.
     * Keys are supposed to be in the form of <classname>:<query>
     *
     * @param <T>
     * @param key
     * @return Class for the given key or null if the key does not map to any class
     */
    @SuppressWarnings("unchecked")
    protected <T extends Content> Class<T> getKeyClass(String key) {
        String className = key.split(":")[0];
        if (log.isDebugEnabled()) {
            log.debug("getKeyClass() "+className);
        } // if
        return (Class<T>) getClassForName(className);
    } // getKeyClass()


    /**
     * attach a listener for any changes dealing with classes of the given type.
     *
     * @param cls
     * @param listener
     */
    @Override
    public void addListener(Class<? extends Content> cls, BeanListener listener) {
        synchronized (attachedListeners) {
            List<BeanListener> listeners = attachedListeners.get(cls);
            if (listeners==null) {
                listeners = new ArrayList<BeanListener>();
                attachedListeners.put(cls, listeners);
            } // if
            listeners.add(listener);
        } // synchronized
        if (log.isInfoEnabled()) {
            log.info("addListener() "+cls.getSimpleName()+": "+attachedListeners.get(cls).size());
        } // if
    } // addListener()


    /**
     * Filter a list of objects for instances of a given class.
     *
     * No instances of subclasses are inserted into the filtered result.
     *
     * @param <T>
     * @param cls          type to filter for
     * @param rawList
     * @param filteredList
     */
    @SuppressWarnings("unchecked")
    protected <T extends Content> void filterExactClass(Class<T> cls, List<? extends Object> rawList, List<T> filteredList) {
        for (Object o : rawList) {
            Class<? extends Object> instanceClass = o.getClass();
            // eliminate problems with JPA sublcassing at runtime
            if (instanceClass.getName().startsWith("org.apache.openjpa.enhance")) {
                instanceClass = instanceClass.getSuperclass();
            } // if
            if (o instanceof Content) {
                Content c = (Content) o;
                if (instanceClass.isAssignableFrom(cls)) {
                    filteredList.add((T) c);
                } else {
                    if (log.isWarnEnabled()) {
                        log.warn("listBeansOfExactClass() class name of instance "+c.getClass().getName());
                    } // if
                } // if
            } // if
        } // for
    } // filterExactClass()


    protected abstract <T extends Content> T getBean(Class<T> cls, String kind, String internalId) throws Exception;


    @Override
    @SuppressWarnings("unchecked")
    public <T extends Content> T getBean(Class<T> cls, String id) {
        if (activateCaching&&(cache.containsKey(id))) {
            statistics.increase("get bean cached");
            return (T) cache.get(id);
        } // if
        T result = null;
        try {
            String kind = null;
            String internalId = null;
            int idx = id.indexOf(':');
            if (idx>0) {
                kind = id.substring(0, idx);
                internalId = id.substring(idx+1);
            } // if
            result = getBean(cls, kind, internalId);
            if (activateCaching) {
                cache.put(id, result);
            } // if
        } catch (Exception e) {
            if (log.isWarnEnabled()) {
                String simpleName = e.getClass().getSimpleName();
                log.warn("getBean() object not found for id '"+id+"' "+simpleName+": "+e.getLocalizedMessage(), e);
            } // if
        } // try/catch/finally
        statistics.increase("get bean uncached");
        return result;
    } // getBean()


    protected List<Class<? extends Content>> getImplementingClassesForModelClass(Class<? extends Content> baseClass) {
        List<Class<? extends Content>> result = new ArrayList<Class<? extends Content>>();

        for (Class<? extends Content> c : getClasses()) {
            if (baseClass.isAssignableFrom(c)) {
                result.add(c);
            } // if
        } // for

        return result;
    } // getImplementingClassesForModelClass()


    /**
     * just to support JSP's weak calling of methods. It does not allow any parameters.
     *
     * @return map to map abstract classes to all non-abstract classes implementing/extending them
     */
    @Override
    public Map<Class<? extends Content>, List<Class<? extends Content>>> getImplementingClassesMap() {
        if (implementingClassesMap==null) {
            implementingClassesMap = new HashMap<Class<? extends Content>, List<Class<? extends Content>>>();

            // Add the very basic root classes directly here - they won't get auto detected otherwise
            implementingClassesMap.put(getBaseClass(), getImplementingClassesForModelClass(getBaseClass()));
            implementingClassesMap.put(Content.class, getImplementingClassesForModelClass(Content.class));
            for (Class<? extends Content> c : getAllClasses()) {
                implementingClassesMap.put(c, getImplementingClassesForModelClass(c));
            } // for
        } // if
        return implementingClassesMap;
    } // getImplementingClassMap()


    /**
     * Get the classes implementing a given baseClass.
     *
     * @param <T>
     * @param baseClass
     * @return list of non-abstract classes that can be assigned to the given base class
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Content> List<Class<T>> getImplementingClasses(Class<T> baseClass) {
        List<Class<T>> result = new ArrayList<Class<T>>();
        for (Class<? extends Content> c : getImplementingClassesMap().get(baseClass)) {
            result.add((Class<T>) c);
        } // for
        return result;
    } // getImplementingClasses()

} // AbstractMutableBeanFactory
