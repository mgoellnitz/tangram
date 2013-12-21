/**
 *
 * Copyright 2013 Martin Goellnitz
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
import org.tangram.mutable.MutableContent;
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

    protected List<Class<? extends MutableContent>> modelClasses = null;

    protected List<Class<? extends MutableContent>> allClasses = null;

    protected Map<String, Class<? extends MutableContent>> tableNameMapping = null;

    protected Map<Class<? extends Content>, List<Class<? extends MutableContent>>> implementingClassesMap = null;

    protected Map<String, Content> cache = new HashMap<String, Content>();

    private boolean activateCaching = false;

    private Map<Object, Object> configOverrides = null;

    private boolean activateQueryCaching = false;

    private Set<String> basePackages;

    private Map<String, List<String>> queryCache = new HashMap<String, List<String>>();


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


    public JpaBeanFactoryImpl() {
        basePackages = new HashSet<String>();
        basePackages.add("org.tangram.jpa");
    } // JpaBeanFactoryImpl()


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


    protected Object getPrimaryKey(String internalId, Class<? extends Content> kindClass) {
        return internalId;
    } // getPrimaryKey()


    @Override
    @SuppressWarnings("unchecked")
    public <T extends Content> T getBean(Class<T> cls, String id) {
        if (activateCaching&&(cache.containsKey(id))) {
            statistics.increase("get bean cached");
            return (T) cache.get(id);
        } // if
        T result = null;
        try {
            if (modelClasses==null) {
                getClasses();
            } // if
            String kind = null;
            String internalId = null;
            int idx = id.indexOf(':');
            if (idx>0) {
                kind = id.substring(0, idx);
                internalId = id.substring(idx+1);
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
            result = (T) manager.find(kindClass, getPrimaryKey(internalId, kindClass));

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


    /**
     * remember that the newly created bean has to be persisted in the now open transaction!
     *
     * @param <>> type of bean to create
     * @param cls type of bean to create
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @Override
    public <T extends MutableContent> T createBean(Class<T> cls) throws InstantiationException, IllegalAccessException {
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
                if (log.isInfoEnabled()) {
                    log.info("clearCacheFor("+key+") key class"+c);
                } // if
                boolean assignableFrom = c.isAssignableFrom(cls);
                if (log.isInfoEnabled()) {
                    log.info("clearCacheFor("+key+") "+c.getSimpleName()+"? "+assignableFrom);
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
    public <T extends MutableContent> boolean persistUncommitted(T bean) {
        boolean result = false;
        try {
            manager.persist(bean);
            clearCacheFor(bean.getClass());
            result = true;
        } catch (Exception e) {
            log.error("persist()", e);
            if (manager!=null) {
                // yes we saw situations where this was not the case thus hiding other errors!
                rollbackTransaction();
            } // if
        } // try/catch/finally
        return result;
    } // persist()


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
            for (Object o : results) {
                Class<? extends Object> instanceClass = o.getClass();
                // eliminate problems with JPA sublcassing at runtime
                if (instanceClass.getName().startsWith("org.apache.openjpa.enhance")) {
                    instanceClass = instanceClass.getSuperclass();
                } // if
                if (o instanceof Content) {
                    Content c = (Content) o;
                    if (instanceClass.isAssignableFrom(cls)) {
                        result.add((T) c);
                    } else {
                        if (log.isWarnEnabled()) {
                            log.warn("listBeansOfExactClass() class name of instance "+c.getClass().getName());
                        } // if
                    } // if
                } // if
            } // for
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
    public Collection<Class<? extends MutableContent>> getAllClasses() {
        synchronized (this) {
            if (allClasses==null) {
                allClasses = new ArrayList<Class<? extends MutableContent>>();
                tableNameMapping = new HashMap<String, Class<? extends MutableContent>>();

                try {
                    List<String> classNames = startupCache.get(getClassNamesCacheKey(), List.class);
                    if (classNames==null) {
                        ClassResolver resolver = new ClassResolver(basePackages);
                        classNames = new ArrayList<String>();
                        for (Class<? extends MutableContent> cls : resolver.getAnnotatedSubclasses(JpaContent.class, Entity.class)) {
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
                        classNames = new ArrayList<String>();
                        startupCache.put(getClassNamesCacheKey(), classNames);
                    } else {
                        // re-fill runtimes caches from persistence startup cache
                        for (String beanClassName : classNames) {
                            Class<? extends MutableContent> cls = (Class<? extends MutableContent>) Class.forName(beanClassName);
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
    public Collection<Class<? extends MutableContent>> getClasses() {
        synchronized (this) {
            if (modelClasses==null) {
                modelClasses = new ArrayList<Class<? extends MutableContent>>();
                for (Class<? extends MutableContent> cls : getAllClasses()) {
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
                tableNameMapping = new HashMap<String, Class<? extends MutableContent>>();
                for (Class<? extends MutableContent> mc : modelClasses) {
                    log.info("getClasses() setting table mapping for "+mc);
                    tableNameMapping.put(mc.getSimpleName(), mc);
                    // TODO: doesn't help much
                    /*
                     log.info("getClasses() adding meta data for "+mc);
                     OpenJPAConfiguration configuration = ((OpenJPAEntityManagerFactory)getManager().getEntityManagerFactory()).getConfiguration();
                     configuration.getMetaDataRepositoryInstance().addMetaData(mc);
                     */
                } // for
            } // if
        } // if
        return modelClasses;
    } // getClasses()


    private Collection<Class<? extends MutableContent>> additionalClasses = Collections.emptySet();


    @Override
    public void setAdditionalClasses(Collection<Class<? extends MutableContent>> classes) {
        Set<Class<? extends MutableContent>> classSet = new HashSet<Class<? extends MutableContent>>();
        if (classes!=null) {
            for (Class<? extends MutableContent> cls : classes) {
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


    /**
     * just to support JSP weak calling of methods with no parameters
     *
     * @param baseClass
     * @return
     */
    @Override
    public Map<Class<? extends Content>, List<Class<? extends MutableContent>>> getImplementingClassesMap() {
        if (implementingClassesMap==null) {
            implementingClassesMap = new HashMap<Class<? extends Content>, List<Class<? extends MutableContent>>>();

            // Add the very basic root classes directly here - they won't get auto detected otherwise
            implementingClassesMap.put(JpaContent.class, getImplementingClassesForModelClass(JpaContent.class));
            implementingClassesMap.put(Content.class, getImplementingClassesForModelClass(Content.class));
            for (Class<? extends Content> c : getAllClasses()) {
                implementingClassesMap.put(c, getImplementingClassesForModelClass(c));
            } // for
        } // if
        return implementingClassesMap;
    } // getImplementingClassMap()


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
        final Collection<Class<? extends MutableContent>> classes = getAllClasses();
        // OpenJPA specific class handling to be able to handle classes from the class repository
        StringBuilder classList = new StringBuilder("org.tangram.jpa.JpaContent");
        for (Class<? extends MutableContent> c : classes) {
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
                    log.info("afterPropertiesSet() manager factory: "+entity.getName()+"/"+entity.getJavaType().getName());
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
