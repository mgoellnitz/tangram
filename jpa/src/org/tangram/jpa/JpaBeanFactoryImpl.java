/**
 *
 * Copyright 2013-2016 Martin Goellnitz
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
public class JpaBeanFactoryImpl extends AbstractMutableBeanFactory<EntityManager, Query> implements MutableBeanFactory<EntityManager, Query> {

    private static final Logger LOG = LoggerFactory.getLogger(JpaBeanFactoryImpl.class);

    private String persistenceUnitName = "tangram";

    private EntityManagerFactory managerFactory = null;

    private EntityManager manager = null;

    protected List<Class<? extends Content>> allClasses = null;

    private Map<Object, Object> configOverrides = null;


    public JpaBeanFactoryImpl() {
        LOG.info("()");
    } // ()


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
     * Override Entity Manager Factory properties given in persistence.xml.
     * The values added here override and add to the values from persistence.xml.
     * The details of the values are to be taken from the documentation of the JPA implementation.
     *
     * @param configOverrides name value pair mapping with values for the given JPA implementation.
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
    public Query createQuery(Class<? extends Content> cls, String queryString) {
        String simpleName = cls.getSimpleName();
        return manager.createQuery(queryString==null ? "select x from "+simpleName+" x" : queryString, cls);
    } // createQuery()


    @Override
    public <T extends Content> List<T> listBeans(Class<T> c, String query, String orderProperty, Boolean ascending) {
        List<T> result = new ArrayList<>();
        try {
            for (Class<T> cls : getImplementingBaseClasses(c)) {
                String simpleName = cls.getSimpleName();
                String queryString = query==null ? "select x from "+simpleName+" x" : query;
                // Default is no ordering - not even via IDs
                if (orderProperty!=null) {
                    String boundName = queryString.substring(7);
                    int idx = boundName.indexOf(' ');
                    boundName = boundName.substring(0, idx);
                    queryString += " order by "+boundName+"."+orderProperty+(ascending ? " asc" : " desc");
                } // if
                LOG.info("listBeans() looking up instances of {} with condition {}", simpleName, queryString);
                Query q = manager.createQuery(queryString, cls);
                result.addAll(SystemUtils.convert(q.getResultList()));
            } // for
            LOG.info("listBeans() looked up {} raw entries", result.size());
            statistics.increase("list beans");
        } catch (Exception e) {
            LOG.error("listBeans() query ", e);
        } // try/catch/finally
        return result;
    } // listBeans()


    @Override
    public <T extends Content> List<T> listBeans(Query query) {
        List<T> result = new ArrayList<>();
        try {
            LOG.info("listBeans() looking up instances with query {}", query);
            result.addAll(SystemUtils.convert(query.getResultList()));
            LOG.info("listBeans() looked up {} raw entries", result.size());
            statistics.increase("list beans");
        } catch (Exception e) {
            LOG.error("listBeans() query ", e);
        } // try/catch/finally
        return result;
    } // listBeans()


    @Override
    public Collection<Class<? extends Content>> getAllClasses() {
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
        return allClasses;
    } // getAllClasses()


    @Override
    public <F extends Content> String getFilterQuery(Class<F> cls, String filterProperty, String filterValues) {
        String baseClause = super.getFilterQuery(cls, "f."+filterProperty, filterValues);
        return "select f from "+cls.getSimpleName()+" f WHERE "+baseClause;
    } // getFilterQuery()


    @Override
    public EntityManager getManager() {
        return manager;
    } // getManager()


    protected Map<? extends Object, ? extends Object> getFactoryConfigOverrides() {
        return getConfigOverrides()==null ? Collections.emptyMap() : getConfigOverrides();
    } // getFactoryConfigOverrides()


    /**
     * Initializes the bean factory after JPA manager and factory have been obtained.
     *
     * initializes the queryCache from the startupCache if possible and re-calibrates the available entity list.
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
        LOG.debug("afterPropertiesSet() using overrides for entity manager factory: {}", overrides);

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
        setEntityManagerFactory(Persistence.createEntityManagerFactory(persistenceUnitName, properties));
        setEntityManager(managerFactory.createEntityManager());
        initFactory();
    } // afterPropertiesSet()

} // JpaBeanFactoryImpl
