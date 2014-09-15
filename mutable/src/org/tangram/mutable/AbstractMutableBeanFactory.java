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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.PersistentRestartCache;
import org.tangram.content.AbstractBeanFactory;
import org.tangram.content.BeanFactoryAware;
import org.tangram.content.BeanListener;
import org.tangram.content.Content;
import org.tangram.monitor.Statistics;
import org.tangram.util.ClassResolver;


/**
 * Common stuff for all bean factories dealing with mutable content.
 */
public abstract class AbstractMutableBeanFactory extends AbstractBeanFactory implements MutableBeanFactory {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractMutableBeanFactory.class);

    protected static final String QUERY_CACHE_KEY = "tangram.query.cache";

    @Inject
    protected Statistics statistics;

    @Inject
    protected PersistentRestartCache startupCache;

    private final Map<Class<? extends Content>, List<BeanListener>> attachedListeners = new HashMap<>();

    private Set<String> basePackages;

    /**
     * mapping from classes or interfaces to non abstract classes implementing them
     */
    protected Map<Class<? extends Content>, List<Class<? extends Content>>> implementingClassesMap = null;

    protected List<Class<? extends Content>> modelClasses = null;

    protected Map<String, Class<? extends Content>> tableNameMapping = null;

    protected Map<String, Content> cache = new HashMap<>();

    private boolean activateCaching = false;

    protected Map<String, List<String>> queryCache = new HashMap<>();

    private boolean activateQueryCaching = false;


    public AbstractMutableBeanFactory() {
        basePackages = new HashSet<>();
        basePackages.add(getBaseClass().getPackage().getName());
    } // AbstractMutableBeanFactory()


    public Set<String> getBasePackages() {
        return basePackages;
    }


    public void setBasePackages(Set<String> basePackages) {
        this.basePackages = basePackages;
    }


    public boolean isActivateCaching() {
        return activateCaching;
    }


    public void setActivateCaching(boolean activateCaching) {
        this.activateCaching = activateCaching;
    }


    public boolean isActivateQueryCaching() {
        return activateQueryCaching;
    }


    public void setActivateQueryCaching(boolean activateQueryCaching) {
        this.activateQueryCaching = activateQueryCaching;
    }


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
     * remember that the newly created bean has to be persisted in the now open transaction!
     *
     * @param <T> type of bean to create
     * @param cls instance of that type
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @Override
    public <T extends Content> T createBean(Class<T> cls) throws InstantiationException, IllegalAccessException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createBean() beginning transaction");
        } // if
        beginTransaction();

        if (LOG.isDebugEnabled()) {
            LOG.debug("createBean() creating new instance of "+cls.getName());
        } // if
        T bean = cls.newInstance();

        statistics.increase("create bean");
        return bean;
    } // createBean()


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
            LOG.error("persistUncommitted()", e);
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
            LOG.error("delete()", e);
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
     * @return resulting class or null if not possible (should never happen...)
     */
    @SuppressWarnings("unchecked")
    protected <T extends Content> Class<T> getClassForName(String className) {
        Class<T> result = null;
        for (Class<? extends Content> c : getClasses()) {
            if (c.getName().equals(className)) {
                result = (Class<T>) c;
            } // if
        } // for
        if (result==null) {
            try {
                result = ClassResolver.loadClass(className);
            } catch (ClassNotFoundException cnfe) {
                LOG.error("getClassForName()", cnfe);
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
    protected <T extends Content> Class<T> getKeyClass(String key) {
        String className = key.split(":")[0];
        if (LOG.isDebugEnabled()) {
            LOG.debug("getKeyClass() "+className);
        } // if
        return getClassForName(className);
    } // getKeyClass()


    @Override
    public void clearCacheFor(Class<? extends Content> cls) {
        statistics.increase("bean cache clear");
        cache.clear();
        if (LOG.isInfoEnabled()) {
            LOG.info("clearCacheFor() "+cls.getName());
        } // if
        try {
            // clear query cache first since listeners might want to use query to obtain fresh data
            Collection<String> removeKeys = new HashSet<>();
            for (Object keyObject : queryCache.keySet()) {
                String key = (String) keyObject;
                Class<? extends Content> c = getKeyClass(key);
                boolean assignableFrom = c.isAssignableFrom(cls);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("clearCacheFor("+key+") "+c.getSimpleName()+"? "+assignableFrom);
                } // if
                if (assignableFrom) {
                    removeKeys.add(key);
                } // if
            } // for
            for (String key : removeKeys) {
                queryCache.remove(key);
            } // for
            startupCache.put(QUERY_CACHE_KEY, queryCache);

            for (Class<? extends Content> c : getListeners().keySet()) {
                boolean assignableFrom = c.isAssignableFrom(cls);
                if (LOG.isInfoEnabled()) {
                    LOG.info("clearCacheFor() "+c.getSimpleName()+"? "+assignableFrom);
                } // if
                if (assignableFrom) {
                    List<BeanListener> listeners = getListeners().get(c);
                    if (LOG.isInfoEnabled()) {
                        LOG.info("clearCacheFor() triggering "+(listeners==null ? "no" : listeners.size())+" listeners");
                    } // if
                    if (listeners!=null) {
                        for (BeanListener listener : listeners) {
                            listener.reset();
                        } // for
                    } // if
                } // if
            } // for
        } catch (Exception e) {
            LOG.error("clearCacheFor() "+cls.getSimpleName(), e);
        } // try/catch
    } // clearCacheFor()


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
                listeners = new ArrayList<>();
                attachedListeners.put(cls, listeners);
            } // if
            listeners.add(listener);
        } // synchronized
        if (LOG.isInfoEnabled()) {
            LOG.info("addListener() "+cls.getSimpleName()+": "+attachedListeners.get(cls).size());
        } // if
    } // addListener()


    /**
     * Filter a list of objects for instances of a given class.
     *
     * No instances of subclasses are inserted into the filtered result.
     *
     * @param <T>
     * @param cls type to filter for
     * @param rawList
     * @param filteredList
     */
    @SuppressWarnings("unchecked")
    protected <T extends Content> void filterExactClass(Class<T> cls, List<? extends Object> rawList, List<T> filteredList) {
        for (Object o : rawList) {
            Class<? extends Object> instanceClass = o.getClass();
            // eliminate problems with JPA subclassing at runtime
            if (instanceClass.getName().startsWith("org.apache.openjpa.enhance")) {
                instanceClass = instanceClass.getSuperclass();
            } // if
            if (instanceClass.isAssignableFrom(cls)) {
                if (o instanceof BeanFactoryAware) {
                    ((BeanFactoryAware) o).setBeanFactory(this);
                } // if
                filteredList.add((T) o);
            } else {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("filterExactClass() class name of instance "+o.getClass().getName());
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
            if (result instanceof BeanFactoryAware) {
                ((BeanFactoryAware) result).setBeanFactory(this);
            } // if
            if (activateCaching) {
                cache.put(id, result);
            } // if
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
                String simpleName = e.getClass().getSimpleName();
                LOG.warn("getBean() object not found for id '"+id+"' "+simpleName+": "+e.getLocalizedMessage(), e);
            } // if
        } // try/catch/finally
        statistics.increase("get bean uncached");
        return result;
    } // getBean()


    protected List<Class<? extends Content>> getImplementingClassesForModelClass(Class<? extends Content> baseClass) {
        List<Class<? extends Content>> result = new ArrayList<>();

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
            implementingClassesMap = new HashMap<>();

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
        List<Class<T>> result = new ArrayList<>();
        for (Class<? extends Content> c : getImplementingClassesMap().get(baseClass)) {
            result.add((Class<T>) c);
        } // for
        return result;
    } // getImplementingClasses()


    private <T> String getCacheKey(Class<T> cls, String queryString, String orderProperty, Boolean ascending) {
        return cls.getName()+":"+orderProperty+":"+(ascending==Boolean.TRUE ? "asc" : "desc")+":"+queryString;
    } // getCacheKey()


    @Override
    public <T extends Content> List<T> listBeans(Class<T> cls, String queryString, String orderProperty, Boolean ascending) {
        List<T> result = null;
        if (LOG.isInfoEnabled()) {
            LOG.info("listBeans() looking up instances of "+cls.getSimpleName()+(queryString==null ? "" : " with condition "+queryString));
        } // if
        String key = null;
        if (isActivateQueryCaching()) {
            key = getCacheKey(cls, queryString, orderProperty, ascending);
            List<String> idList = queryCache.get(key);
            if (idList!=null) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("listBeans() found in cache "+idList);
                } // if
                // old style
                result = new ArrayList<>(idList.size());
                for (String id : idList) {
                    result.add(getBean(cls, id));
                } // for
                // New style with lazy content list - perhaps will work some day
                // result = new LazyContentList<T>(this, idList);
                statistics.increase("query beans cached");
            } // if
        } // if
        if (result==null) {
            result = new ArrayList<>();
            for (Class<? extends Content> cx : getClasses()) {
                if (cls.isAssignableFrom(cx)) {
                    @SuppressWarnings("unchecked")
                    Class<? extends T> c = (Class<? extends T>) cx;
                    List<? extends T> beans = listBeansOfExactClass(c, queryString, orderProperty, ascending);
                    result.addAll(beans);
                } // if
            } // for
            if (isActivateQueryCaching()) {
                List<String> idList = new ArrayList<>(result.size());
                for (T content : result) {
                    idList.add(content.getId());
                } // for
                queryCache.put(key, idList);
                startupCache.put(QUERY_CACHE_KEY, queryCache);
            } // if
            statistics.increase("query beans uncached");
        } // if
        if (LOG.isInfoEnabled()) {
            LOG.info("listBeans() looked up "+result.size()+" raw entries");
        } // if
        return result;
    } // listBeans()


    @Override
    public Collection<Class<? extends Content>> getClasses() {
        synchronized (this) {
            if (modelClasses==null) {
                modelClasses = new ArrayList<>();
                for (Class<? extends Content> cls : getAllClasses()) {
                    if ((cls.getModifiers()&Modifier.ABSTRACT)==0) {
                        modelClasses.add(cls);
                    } // if
                } // for
                Comparator<Class<?>> comp = new Comparator<Class<?>>() {

                    @Override
                    public int compare(Class<?> o1, Class<?> o2) {
                        return o1.getName().compareTo(o2.getName());
                    } // compareTo()

                };
                Collections.sort(modelClasses, comp);
                tableNameMapping = new HashMap<>();
                for (Class<? extends Content> mc : modelClasses) {
                    tableNameMapping.put(mc.getSimpleName(), mc);
                } // for
            } // if
        } // synchronized
        return modelClasses;
    } // getClasses()

} // AbstractMutableBeanFactory
