/**
 *
 * Copyright 2011-2014 Martin Goellnitz
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
package org.tangram.jdo;

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
import javax.annotation.PostConstruct;
import javax.jdo.Extent;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.annotations.PersistenceCapable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.content.BeanListener;
import org.tangram.content.Content;
import org.tangram.mutable.AbstractMutableBeanFactory;
import org.tangram.util.ClassResolver;


public abstract class AbstractJdoBeanFactory extends AbstractMutableBeanFactory implements JdoBeanFactory {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractJdoBeanFactory.class);

    protected PersistenceManagerFactory managerFactory = null;

    protected PersistenceManager manager = null;

    protected List<Class<? extends Content>> allClasses = null;

    /**
     * non abstract classes for storaable mutable data models
     */
    protected List<Class<? extends Content>> modelClasses = null;

    protected Map<String, Class<? extends Content>> tableNameMapping = null;

    private Map<Object, Object> configOverrides = null;

    private Set<String> basePackages;

    private boolean activateQueryCaching = false;

    private boolean prefill = true;

    private Map<String, List<String>> queryCache = new HashMap<String, List<String>>();


    @Override
    public PersistenceManager getManager() {
        return manager;
    }


    public AbstractJdoBeanFactory() {
        basePackages = new HashSet<String>();
        basePackages.add("org.tangram");
    } // AbstractJdoBeanFactory()


    public Set<String> getBasePackages() {
        return basePackages;
    }


    public void setBasePackages(Set<String> basePackages) {
        this.basePackages = basePackages;
    }


    public Map<Object, Object> getConfigOverrides() {
        return configOverrides;
    }


    /**
     *
     * Override Persistence Manager Factory properties given in jdoconfig.xml
     *
     * @param configOverrides
     */
    public void setConfigOverrides(Map<Object, Object> configOverrides) {
        this.configOverrides = configOverrides;
    }


    public boolean isActivateQueryCaching() {
        return activateQueryCaching;
    }


    public void setActivateQueryCaching(boolean activateQueryCaching) {
        this.activateQueryCaching = activateQueryCaching;
    }


    public boolean isPrefill() {
        return prefill;
    }


    public void setPrefill(boolean prefill) {
        this.prefill = prefill;
    }


    protected abstract Object getObjectId(String internalId, Class<? extends Content> kindClass, String kind);


    @Override
    @SuppressWarnings("unchecked")
    protected <T extends Content> T getBean(Class<T> cls, String kind, String internalId) throws Exception {
        if (modelClasses==null) {
            getClasses();
        } // if
        Class<? extends Content> kindClass = tableNameMapping.get(kind);
        if (kindClass==null) {
            throw new Exception("Passed over kind "+kind+" not valid");
        } // if
        if (!(cls.isAssignableFrom(kindClass))) {
            throw new Exception("Passed over class "+cls.getSimpleName()+" does not match "+kindClass.getSimpleName());
        } // if
        Object oid = getObjectId(internalId, kindClass, kind);
        if (LOG.isInfoEnabled()) {
            LOG.info("getBean() "+kindClass.getName()+" "+internalId+" oid="+oid);
        } // if
        return (T) manager.getObjectById(kindClass, oid);
    } // getBean()


    @Override
    public JdoContent getBean(String id) {
        return getBean(JdoContent.class, id);
    } // getBean()


    @Override
    public void beginTransaction() {
        if (!manager.currentTransaction().isActive()) {
            manager.currentTransaction().begin();
        } // if
    } // beginTransaction()


    @Override
    public void commitTransaction() {
        if (manager.currentTransaction().isActive()) {
            manager.currentTransaction().commit();
        } // if
    } // commitTransaction()


    @Override
    public void rollbackTransaction() {
        if (manager.currentTransaction().isActive()) {
            manager.currentTransaction().commit();
        } // if
    } // rollbackTransaction()


    @Override
    protected boolean hasManager() {
        return manager!=null;
    } // hasManager()


    @Override
    protected <T extends Content> void apiPersist(T bean) {
        manager.makePersistent(bean);
    } // apiPersist()


    @Override
    protected <T extends Content> void apiDelete(T bean) {
        manager.deletePersistent(bean);
    } // apiDelete()


    /**
     * remember that the newly created bean has to be persisted in the now open transaction!
     *
     * @see JdoBeanFactory
     */
    @Override
    public <T extends Content> T createBean(Class<T> cls) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createBean() beginning transaction");
        } // if
        beginTransaction();

        if (LOG.isDebugEnabled()) {
            LOG.debug("createBean() creating new instance of "+cls.getName());
        } // if
        T bean = manager.newInstance(cls);

        statistics.increase("create bean");
        return bean;
    } // createBean()


    @Override
    @SuppressWarnings("unchecked")
    public <T extends Content> List<T> listBeansOfExactClass(Class<T> cls, String queryString, String orderProperty, Boolean ascending) {
        List<T> result = new ArrayList<T>();
        try {
            Extent<T> extent = manager.getExtent(cls, false);
            Query query = queryString==null ? manager.newQuery(extent) : manager.newQuery(extent, queryString);
            // Default is no ordering - not even via IDs
            if (orderProperty!=null) {
                String order = orderProperty+((ascending==Boolean.TRUE) ? " asc" : " desc");
                query.setOrdering(order);
            } // if
            // TOOD: will be extended once we decide to introduce start/end
            // if (end!=null) {
            // long from = start!=null ? start : 0;
            // query.setRange(from, end+1);
            // } // if
            if (LOG.isInfoEnabled()) {
                LOG.info("listBeansOfExactClass() looking up instances of "+cls.getSimpleName()
                        +(queryString==null ? "" : " with condition "+queryString));
            } // if
            List<T> results = (List<T>) query.execute();
            if (LOG.isInfoEnabled()) {
                LOG.info("listBeansOfExactClass() looked up "+results.size()+" raw entries");
            } // if
            for (T o : results) {
                result.add(o);
            } // for
            statistics.increase("list beans");
        } catch (Exception e) {
            LOG.error("listBeansOfExactClass() query ", e);
        } // try/catch/finally
        return result;
    } // listBeansOfExactClass()


    private <T> String getCacheKey(Class<T> cls, String queryString, String orderProperty, Boolean ascending) {
        return cls.getName()+":"+orderProperty+":"+(ascending==Boolean.TRUE ? "asc" : "desc")+":"+queryString;
    } // getCacheKey()


    @Override
    public <T extends Content> List<T> listBeans(Class<T> cls, String queryString, String orderProperty, Boolean ascending) {
        List<T> result = null;
        if (LOG.isInfoEnabled()) {
            LOG.info("listBeans() looking up instances of "+cls.getSimpleName()
                    +(queryString==null ? "" : " with condition "+queryString));
        } // if
        String key = null;
        if (activateQueryCaching) {
            key = getCacheKey(cls, queryString, orderProperty, ascending);
            List<String> idList = queryCache.get(key);
            if (idList!=null) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("listBeans() found in cache "+idList);
                } // if
                // old style
                result = new ArrayList<T>(idList.size());
                for (String id : idList) {
                    result.add(getBean(cls, id));
                } // for

                // New style with lazy content list - perhaps will work some day
                // result = new LazyContentList<T>(this, idList);
                statistics.increase("query beans cached");
            } // if
        } // if
        if (result==null) {
            result = new ArrayList<T>();
            for (Class<? extends Content> cx : getClasses()) {
                if (cls.isAssignableFrom(cx)) {
                    @SuppressWarnings("unchecked")
                    Class<? extends T> c = (Class<? extends T>) cx;
                    List<? extends T> beans = listBeansOfExactClass(c, queryString, orderProperty, ascending);
                    result.addAll(beans);
                } // if
            } // for
            if (activateQueryCaching) {
                List<String> idList = new ArrayList<String>(result.size());
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
    public void clearCacheFor(Class<? extends Content> cls) {
        statistics.increase("bean cache clear");
        cache.clear();
        if (LOG.isInfoEnabled()) {
            LOG.info("clearCacheFor() "+cls.getSimpleName());
        } // if
        try {
            // clear query cache first since listeners might want to use query to obtain fresh data
            Collection<String> removeKeys = new HashSet<String>();
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
     * Get a collection of model related classes.
     *
     * This method also returns the abstract classes or interfaces in the base packages.
     *
     * @return collection with all classes
     */
    @Override
    @SuppressWarnings("unchecked")
    public Collection<Class<? extends Content>> getAllClasses() {
        synchronized (this) {
            if (allClasses==null) {
                allClasses = new ArrayList<Class<? extends Content>>();
                try {
                    List<String> classNames = startupCache.get(getClassNamesCacheKey(), List.class);
                    if (classNames==null) {
                        ClassResolver resolver = new ClassResolver(basePackages);
                        classNames = new ArrayList<String>();
                        for (Class<? extends Content> cls : resolver.getAnnotatedSubclasses(JdoContent.class, PersistenceCapable.class)) {
                            if (LOG.isInfoEnabled()) {
                                LOG.info("getAllClasses() * "+cls.getName());
                            } // if
                            classNames.add(cls.getName());
                            // table name mapping moved to getClasses()
                            allClasses.add(cls);
                        } // for
                        if (LOG.isInfoEnabled()) {
                            LOG.info("getAllClasses() # class names "+classNames.size());
                        } // if
                        startupCache.put(getClassNamesCacheKey(), classNames);
                    } else {
                        for (String beanClassName : classNames) {
                            Class<? extends Content> cls = (Class<? extends Content>) Class.forName(beanClassName);
                            if (LOG.isInfoEnabled()) {
                                LOG.info("getAllClasses() # "+cls.getName());
                            } // if
                            allClasses.add(cls);
                        } // for
                    } // if
                } catch (Exception e) {
                    LOG.error("getAllClasses() outer", e);
                } // try/catch
            } // if
        } // synchronized
        return allClasses;
    } // getAllClasses()


    @Override
    public Collection<Class<? extends Content>> getClasses() {
        synchronized (this) {
            if (modelClasses==null) {
                modelClasses = new ArrayList<Class<? extends Content>>();
                for (Class<? extends Content> cls : getAllClasses()) {
                    if (!((cls.getModifiers()&Modifier.ABSTRACT)==Modifier.ABSTRACT)) {
                        modelClasses.add(cls);
                    } // if
                } // for
                Comparator<Class<?>> comp = new Comparator<Class<?>>() {

                    @Override
                    public int compare(Class<?> o1, Class<?> o2) {
                        return o1.getName().compareTo(o2.getName());
                    } // compareTo()

                };
                modelClasses.addAll(additionalClasses);
                Collections.sort(modelClasses, comp);
                tableNameMapping = new HashMap<String, Class<? extends Content>>();
                for (Class<? extends Content> mc : modelClasses) {
                    tableNameMapping.put(mc.getSimpleName(), mc);
                } // for
            } // if
        } // if
        return modelClasses;
    } // getClasses()


    private Collection<Class<? extends Content>> additionalClasses = Collections.emptySet();


    @Override
    public void setAdditionalClasses(Collection<Class<? extends Content>> classes) {
        Set<Class<? extends Content>> classSet = new HashSet<Class<? extends Content>>();
        if (classes!=null) {
            for (Class<? extends Content> cls : classes) {
                if (JdoContent.class.isAssignableFrom(cls)) {
                    if (cls.getAnnotation(PersistenceCapable.class)!=null) {
                        if (!((cls.getModifiers()&Modifier.ABSTRACT)==Modifier.ABSTRACT)) {
                            classSet.add(cls);
                        } // if
                    } // if
                } // if
            } // for
        } // if
        additionalClasses = classSet;
        modelClasses = null;
    } // setAdditionalClasses()


    protected Map<? extends Object, ? extends Object> getFactoryConfigOverrides() {
        return getConfigOverrides()==null ? Collections.emptyMap() : getConfigOverrides();
    } // getFactoryConfigOverrides()


    @PostConstruct
    @SuppressWarnings("unchecked")
    public void afterPropertiesSet() throws Exception {
        if (LOG.isInfoEnabled()) {
            LOG.info("afterPropertiesSet() bean factory is using "+getClass().getClassLoader().getClass().getName());
        } // if
        Map<? extends Object, ? extends Object> configOverrides = getFactoryConfigOverrides();
        if (LOG.isInfoEnabled()) {
            LOG.info("afterPropertiesSet() using overrides for persistence manager factory: "+configOverrides);
        } // if
        managerFactory = JDOHelper.getPersistenceManagerFactory(configOverrides, "transactions-optional");
        manager = managerFactory.getPersistenceManager();

        // Just to prefill
        if (prefill) {
            final Collection<Class<? extends Content>> theClasses = getClasses();
            if (LOG.isInfoEnabled()) {
                LOG.info("afterPropertiesSet() prefilling done for "+basePackages+": "+theClasses);
            } // if
        } // if
        Map<String, List<String>> c = startupCache.get(QUERY_CACHE_KEY, queryCache.getClass());
        if (c!=null) {
            queryCache = c;
        } // if
    } // afterPropertiesSet()

} // AbstractJdoBeanFactory
