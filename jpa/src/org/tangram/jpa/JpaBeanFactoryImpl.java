/**
 *
 * Copyright 2013-2015 Martin Goellnitz
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.content.Content;
import org.tangram.content.TransientCode;
import org.tangram.mutable.AbstractMutableBeanFactory;
import org.tangram.mutable.MutableBeanFactory;
import org.tangram.util.ClassResolver;
import org.tangram.util.SystemUtils;


/**
 * Generic BeanFactory implementation for use with Java Persistence API implementations.
 */
@Named("beanFactory")
@Singleton
public class JpaBeanFactoryImpl extends AbstractMutableBeanFactory implements MutableBeanFactory {

    private static final Logger LOG = LoggerFactory.getLogger(JpaBeanFactoryImpl.class);

    private String persistenceUnitName = "tangram";

    private EntityManagerFactory managerFactory = null;

    private EntityManager manager = null;

    protected List<Class<? extends Content>> allClasses = null;

    private Map<Object, Object> configOverrides = null;


    protected void setEntityManagerFactory(EntityManagerFactory factory) {
        managerFactory = factory;
    } // setEntityManagerFactory()


    protected void setEntityManager(EntityManager manager) {
        this.manager = manager;
    } // setEntityManagerFactory()


    @Override
    public Class<? extends Content> getBaseClass() {
        return JpaContent.class;
    } // getBaseClass()


    public Map<Object, Object> getConfigOverrides() {
        return configOverrides;
    }


    /**
     *
     * Override Entity Manager Factory properties given in persistence.xml
     *
     * @param configOverrides
     */
    public void setConfigOverrides(Map<Object, Object> configOverrides) {
        this.configOverrides = configOverrides;
    }


    public String getPersistenceUnitName() {
        return persistenceUnitName;
    }


    public void setPersistenceUnitName(String persistenceUnitName) {
        this.persistenceUnitName = persistenceUnitName;
    }


    protected Object getPrimaryKey(String internalId, Class<? extends Content> kindClass) {
        return Long.parseLong(internalId);
    } // getPrimaryKey()


    @Override
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
        LOG.info("getBean() {}: {}", kindClass.getName(), internalId);
        return convert(cls, manager.find(kindClass, getPrimaryKey(internalId, kindClass)));
    } // getBean()


    @Override
    public Content getBean(String id) {
        return getBean(JpaContent.class, id);
    } // getBean()


    @Override
    public void beginTransaction() {
        if (!manager.getTransaction().isActive()) {
            manager.getTransaction().begin();
        } // if
    } // beginTransaction()


    @Override
    public void commitTransaction() {
        if (manager.getTransaction().isActive()) {
            manager.getTransaction().commit();
        } // if
    } // commitTransaction()


    @Override
    public void rollbackTransaction() {
        if (manager.getTransaction().isActive()) {
            manager.getTransaction().rollback();
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


    @Override
    public <T extends Content> List<T> listBeansOfExactClass(Class<T> cls, String queryString, String orderProperty, Boolean ascending) {
        List<T> result = new ArrayList<>();
        try {
            if (orderProperty!=null) {
                queryString += " order by "+orderProperty+((ascending==Boolean.TRUE) ? " asc" : " desc");
            } // if
            String shortTypeName = cls.getSimpleName();
            // String findAllQuery = "select x from "+shortTypeName+" x where type(x) in ("+shortTypeName+")";
            String findAllQuery = "select x from "+shortTypeName+" x";
            Query query = manager.createQuery(queryString==null ? findAllQuery : queryString, cls);
            // Default is no ordering - not even via IDs
            if (LOG.isInfoEnabled()) {
                LOG.info("listBeansOfExactClass() looking up instances of "+shortTypeName
                        +(queryString==null ? "" : " with condition "+queryString));
            } // if
            List<Object> results = SystemUtils.convert(query.getResultList());
            LOG.info("listBeansOfExactClass() looked up {} raw entries", results.size());
            filterExactClass(cls, results, result);
            statistics.increase("list beans");
        } catch (Exception e) {
            LOG.error("listBeansOfExactClass() query ", e);
        } // try/catch/finally
        return result;
    } // listBeansOfExactClass()


    @Override
    public Collection<Class<? extends Content>> getAllClasses() {
        synchronized (this) {
            if (allClasses==null) {
                allClasses = new ArrayList<>();
                try {
                    List<String> classNames = SystemUtils.convert(startupCache.get(getClassNamesCacheKey(), List.class));
                    if (classNames==null) {
                        ClassResolver resolver = new ClassResolver(getBasePackages());
                        classNames = new ArrayList<>();
                        Set<Class<? extends Content>> resolvedClasses = new HashSet<>();
                        resolvedClasses.addAll(resolver.getAnnotatedSubclasses(getBaseClass(), Entity.class));
                        resolvedClasses.addAll(resolver.getSubclasses(Content.class));
                        for (Class<? extends Content> cls : resolvedClasses) {
                            LOG.info("getAllClasses() * {}", cls.getName());
                            if (!allClasses.contains(cls)) {
                                classNames.add(cls.getName());
                                allClasses.add(cls);
                            } // if
                        } // for
                        LOG.info("getAllClasses() # class names {}", classNames.size());
                        startupCache.put(getClassNamesCacheKey(), classNames);
                    } else {
                        // re-fill runtimes caches from persistence startup cache
                        for (String beanClassName : classNames) {
                            Class<? extends Content> cls = ClassResolver.loadClass(beanClassName);
                            LOG.info("getAllClasses() # {}", cls.getName());
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


    protected Map<? extends Object, ? extends Object> getFactoryConfigOverrides() {
        return getConfigOverrides()==null ? Collections.emptyMap() : getConfigOverrides();
    } // getFactoryConfigOverrides()


    /**
     * Initializes the bean factory after JPA manager and factory have been obtained.
     *
     * intializes the queryCache from the startupCache if possible and re-calibrates the available entity list.
     */
    protected void initFactory() {
        LOG.info("initFactory() manager factory: {}", managerFactory.getClass().getName());
        LOG.info("initFactory() manager: {}", manager.getClass().getName());
        // calibrate classes discovered by annotation with classes found by manager
//        Metamodel metamodel = manager.getMetamodel();
//        if (metamodel!=null) {
//            allClasses = new ArrayList<>();
//            modelClasses = null;
//            Set<EntityType<?>> entities = metamodel.getEntities();
//            for (EntityType<?> entity : entities) {
//                LOG.info("initFactory() discovered entity: "+entity.getName()+"/"+entity.getJavaType().getName());
//                allClasses.add((Class<? extends Content>) entity.getJavaType());
//            } // for
//        } else {
//            LOG.warn("initFactory() not meta model");
//        } // if
        Map<String, List<String>> c = SystemUtils.convert(startupCache.get(QUERY_CACHE_KEY, queryCache.getClass()));
        if (c!=null) {
            queryCache = c;
        } // if
    } // initFactory()


    @PostConstruct
    public void afterPropertiesSet() {
        Map<? extends Object, ? extends Object> overrides = getFactoryConfigOverrides();
        LOG.info("afterPropertiesSet() using overrides for entity manager factory: {}", overrides);

        // OpenJPA specific class handling to be able to handle classes from the class repository
        StringBuilder classList = new StringBuilder(256);
        classList.append(getBaseClass().getName());
        for (Class<? extends Content> c : getAllClasses()) {
            if (!((c==TransientCode.class)||c.isInterface())||c.getName().equals(getBaseClass().getName())) {
                classList.append(';');
                classList.append(c.getName());
            } // if
        } // for
        Properties properties = new Properties();
        properties.putAll(overrides);
        properties.put("openjpa.MetaDataFactory", "jpa(Types="+classList.toString()+")");
        LOG.info("afterPropertiesSet() properties={}", properties);

        // initialize manager
        managerFactory = Persistence.createEntityManagerFactory(persistenceUnitName, properties);
        manager = managerFactory.createEntityManager();
        initFactory();
    } // afterPropertiesSet()

} // JpaBeanFactoryImpl
