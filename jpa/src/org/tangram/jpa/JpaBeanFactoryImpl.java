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
package org.tangram.jpa;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tangram.content.BeanListener;
import org.tangram.content.Content;
import org.tangram.mutable.AbstractMutableBeanFactory;
import org.tangram.util.ClassResolver;


/**
 * BeanFactory implementation for use with Java Persistence API.
 *
 * For the moment this is either an OpenJPA or an EclipseLink specific solution dependending on the
 * comments in the build file.
 */
public class JpaBeanFactoryImpl extends AbstractMutableBeanFactory implements JpaBeanFactory {

    private static final Log log = LogFactory.getLog(JpaBeanFactoryImpl.class);

    private String persistenceUnitName = "tangram";

    protected EntityManagerFactory managerFactory = null;

    protected EntityManager manager = null;

    protected List<Class<? extends Content>> modelClasses = null;

    protected List<Class<? extends Content>> allClasses = null;

    protected Map<String, Class<? extends Content>> tableNameMapping = null;

    private Map<Object, Object> configOverrides = null;

    private boolean activateQueryCaching = false;

    private Set<String> basePackages;

    private Map<String, List<String>> queryCache = new HashMap<String, List<String>>();


    public JpaBeanFactoryImpl() {
        basePackages = new HashSet<String>();
        basePackages.add("org.tangram.jpa");
    } // JpaBeanFactoryImpl()


    @Override
    public Class<? extends Content> getBaseClass() {
        return JpaContent.class;
    } // getBaseClass()


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
     * Override Entity Manager Factory properties given in persistence.xml
     *
     * @param configOverrides
     */
    @Inject
    @Named("jpaConfigOverrides")
    public void setConfigOverrides(Map<Object, Object> configOverrides) {
        this.configOverrides = configOverrides;
    }


    public String getPersistenceUnitName() {
        return persistenceUnitName;
    }


    public void setPersistenceUnitName(String persistenceUnitName) {
        this.persistenceUnitName = persistenceUnitName;
    }


    @Override
    public EntityManager getManager() {
        return manager;
    }


    public boolean isActivateQueryCaching() {
        return activateQueryCaching;
    }


    public void setActivateQueryCaching(boolean activateQueryCaching) {
        this.activateQueryCaching = activateQueryCaching;
    }


    protected Object getPrimaryKey(String internalId, Class<? extends Content> kindClass) {
        return internalId;
    } // getPrimaryKey()


    @Override
    @SuppressWarnings("unchecked")
    public <T extends Content> T getBean(Class<T> cls, String kind, String internalId) throws Exception {
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
        if (log.isInfoEnabled()) {
            log.info("getBean() "+kindClass.getName()+":"+internalId);
        } // if
        return (T) manager.find(kindClass, getPrimaryKey(internalId, kindClass));
    } // getBean()


    @Override
    public JpaContent getBean(String id) {
        return getBean(JpaContent.class, id);
    } // getBean()


    @Override
    public void beginTransaction() {
        if (!manager.getTransaction().isActive()) {
            manager.getTransaction().begin();;
        } // if
    } // beginTransaction()


    @Override
    public void commitTransaction() {
        if (manager.getTransaction().isActive()) {
            manager.getTransaction().commit();;
        } // if
    } // commitTransaction()


    @Override
    public void rollbackTransaction() {
        if (manager.getTransaction().isActive()) {
            manager.getTransaction().rollback();;
        } // if
    } // rollbackTransaction()


    @Override
    protected boolean hasManager() {
        return manager!=null;
    } // hasManager()


    @Override
    protected <T extends Content> void apiPersist(T bean) {
        manager.persist(bean);
    } // apiPersist()


    @Override
    protected <T extends Content> void apiDelete(T bean) {
        manager.remove(bean);
    } // apiDelete()


    /**
     * remember that the newly created bean has to be persisted in the now open transaction!
     *
     * @param <>> type of bean to create
     * @param cls type of bean to create
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @Override
    public <T extends Content> T createBean(Class<T> cls) throws InstantiationException, IllegalAccessException {
        if (log.isDebugEnabled()) {
            log.debug("createBean() beginning transaction");
        } // if
        beginTransaction();

        if (log.isDebugEnabled()) {
            log.debug("createBean() creating new instance of "+cls.getName());
        } // if
        T bean = cls.newInstance();

        statistics.increase("create bean");
        return bean;
    } // createBean()


    @Override
    public void clearCacheFor(Class<? extends Content> cls) {
        statistics.increase("bean cache clear");
        cache.clear();
        if (log.isInfoEnabled()) {
            log.info("clearCacheFor() "+cls.getName());
        } // if
        try {
            // clear query cache first since listeners might want to use query to obtain fresh data
            Collection<String> removeKeys = new HashSet<String>();
            for (Object keyObject : queryCache.keySet()) {
                String key = (String) keyObject;
                Class<? extends Content> c = getKeyClass(key);
                boolean assignableFrom = c.isAssignableFrom(cls);
                if (log.isDebugEnabled()) {
                    log.debug("clearCacheFor("+key+") "+c.getSimpleName()+"? "+assignableFrom);
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
                if (log.isInfoEnabled()) {
                    log.info("clearCacheFor() "+c.getSimpleName()+"? "+assignableFrom);
                } // if
                if (assignableFrom) {
                    List<BeanListener> listeners = getListeners().get(c);
                    if (log.isInfoEnabled()) {
                        log.info("clearCacheFor() triggering "+(listeners==null ? "no" : listeners.size())+" listeners");
                    } // if
                    if (listeners!=null) {
                        for (BeanListener listener : listeners) {
                            listener.reset();
                        } // for
                    } // if
                } // if
            } // for
        } catch (Exception e) {
            log.error("clearCacheFor() "+cls.getSimpleName(), e);
        } // try/catch
    } // clearCacheFor()


    @Override
    @SuppressWarnings("unchecked")
    public <T extends Content> List<T> listBeansOfExactClass(Class<T> cls, String queryString, String orderProperty, Boolean ascending) {
        List<T> result = new ArrayList<T>();
        try {
            if (orderProperty!=null) {
                queryString += " order by "+orderProperty+((ascending==Boolean.TRUE) ? " asc" : " desc");
            } // if
            String shortTypeName = cls.getSimpleName();
            // String findAllQuery = "select x from "+shortTypeName+" x where type(x) in ("+shortTypeName+")";
            String findAllQuery = "select x from "+shortTypeName+" x";
            Query query = manager.createQuery(queryString==null ? findAllQuery : queryString, cls);
            // Default is no ordering - not even via IDs
            if (log.isInfoEnabled()) {
                log.info("listBeansOfExactClass() looking up instances of "+shortTypeName
                        +(queryString==null ? "" : " with condition "+queryString));
            } // if
            List<Object> results = query.getResultList();
            if (log.isInfoEnabled()) {
                log.info("listBeansOfExactClass() looked up "+results.size()+" raw entries");
            } // if
            filterExactClass(cls, results, result);
            statistics.increase("list beans");
        } catch (Exception e) {
            log.error("listBeansOfExactClass() query ", e);
        } // try/catch/finally
        return result;
    } // listBeansOfExactClass()


    private <T> String getCacheKey(Class<T> cls, String queryString, String orderProperty, Boolean ascending) {
        return cls.getName()+":"+orderProperty+":"+(ascending==Boolean.TRUE ? "asc" : "desc")+":"+queryString;
    } // getCacheKey()


    @Override
    public <T extends Content> List<T> listBeans(Class<T> cls, String queryString, String orderProperty, Boolean ascending) {
        List<T> result = null;
        if (log.isInfoEnabled()) {
            log.info("listBeans() looking up instances of "+cls.getSimpleName()
                    +(queryString==null ? "" : " with condition "+queryString));
        } // if
        String key = null;
        if (activateQueryCaching) {
            key = getCacheKey(cls, queryString, orderProperty, ascending);
            List<String> idList = queryCache.get(key);
            if (idList!=null) {
                if (log.isInfoEnabled()) {
                    log.info("listBeans() found in cache "+idList);
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
        if (log.isInfoEnabled()) {
            log.info("listBeans() looked up "+result.size()+" raw entries");
        } // if
        return result;
    } // listBeans()


    @Override
    @SuppressWarnings("unchecked")
    public Collection<Class<? extends Content>> getAllClasses() {
        synchronized (this) {
            if (allClasses==null) {
                allClasses = new ArrayList<Class<? extends Content>>();
                tableNameMapping = new HashMap<String, Class<? extends Content>>();

                try {
                    List<String> classNames = startupCache.get(getClassNamesCacheKey(), List.class);
                    if (classNames==null) {
                        ClassResolver resolver = new ClassResolver(basePackages);
                        classNames = new ArrayList<String>();
                        for (Class<? extends Content> cls : resolver.getAnnotatedSubclasses(JpaContent.class, Entity.class)) {
                            if (log.isInfoEnabled()) {
                                log.info("getAllClasses() * "+cls.getName());
                            } // if
                            classNames.add(cls.getName());
                            tableNameMapping.put(cls.getSimpleName(), cls);
                            allClasses.add(cls);
                        } // for
                        if (log.isInfoEnabled()) {
                            log.info("getAllClasses() # class names "+classNames.size());
                        } // if
                        startupCache.put(getClassNamesCacheKey(), classNames);
                    } else {
                        // re-fill runtimes caches from persistence startup cache
                        for (String beanClassName : classNames) {
                            Class<? extends Content> cls = (Class<? extends Content>) Class.forName(beanClassName);
                            if (log.isInfoEnabled()) {
                                log.info("getAllClasses() # "+cls.getName());
                            } // if
                            tableNameMapping.put(cls.getSimpleName(), cls);
                            allClasses.add(cls);
                        } // for
                    } // if
                } catch (Exception e) {
                    log.error("getAllClasses() outer", e);
                } // try/catch
            } // if
        } // synchronized
        return allClasses;
    }// getAllClasses()


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
                    log.info("getClasses() setting table mapping for "+mc);
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
                if (JpaContent.class.isAssignableFrom(cls)) {
                    if (cls.getAnnotation(Entity.class)!=null) {
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
    public void afterPropertiesSet() {
        Map<? extends Object, ? extends Object> configOverrides = getFactoryConfigOverrides();
        if (log.isInfoEnabled()) {
            log.info("afterPropertiesSet() using overrides for entity manager factory: "+configOverrides);
        } // if
        // this was the prefill - right at the moment allways necessary
        final Collection<Class<? extends Content>> classes = getAllClasses();
        // OpenJPA specific class handling to be able to handle classes from the class repository
        StringBuilder classList = new StringBuilder("org.tangram.jpa.JpaContent");
        for (Class<? extends Content> c : classes) {
            classList.append(";");
            classList.append(c.getName());
        } // for
        Properties properties = new Properties();
        properties.putAll(configOverrides);
        properties.put("openjpa.MetaDataFactory", "jpa(Types="+classList.toString()+")");
        if (log.isInfoEnabled()) {
            log.info("afterPropertiesSet() properties="+properties);
        } // if

        // here we go with the basic stuff
        managerFactory = Persistence.createEntityManagerFactory(persistenceUnitName, properties);
        manager = managerFactory.createEntityManager();

        if (log.isInfoEnabled()) {
            log.info("afterPropertiesSet() manager factory: "+managerFactory.getClass().getName());
            Metamodel metamodel = manager.getMetamodel();
            if (metamodel!=null) {
                Set<EntityType<?>> entities = metamodel.getEntities();
                for (EntityType<?> entity : entities) {
                    log.info("afterPropertiesSet() discovered entity: "+entity.getName()+"/"+entity.getJavaType().getName());
                } // for
            } else {
                log.info("afterPropertiesSet() not meta model");
            } // if
        } // if

        Map<String, List<String>> c = startupCache.get(QUERY_CACHE_KEY, queryCache.getClass());
        if (c!=null) {
            queryCache = c;
        } // if
    } // afterPropertiesSet()

} // JpaBeanFactoryImpl
