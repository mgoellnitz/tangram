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
package org.tangram.ebean;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.config.ServerConfig;
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
import javax.persistence.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.content.Content;
import org.tangram.mutable.AbstractMutableBeanFactory;
import org.tangram.util.ClassResolver;


public class EBeanFactoryImpl extends AbstractMutableBeanFactory implements EBeanFactory {

    private static final Logger LOG = LoggerFactory.getLogger(EBeanFactoryImpl.class);

    private ServerConfig serverConfig;

    private EbeanServer server;

    private Transaction currentTransaction = null;

    private Set<String> basePackages;

    protected List<Class<? extends Content>> modelClasses = null;

    protected List<Class<? extends Content>> allClasses = null;

    protected Map<String, Class<? extends Content>> tableNameMapping = null;

    private Map<Object, Object> configOverrides = null;

    private Map<String, List<String>> queryCache = new HashMap<>();

    private boolean activateQueryCaching = false;


    public EBeanFactoryImpl() {
        basePackages = new HashSet<>();
        basePackages.add("org.tangram");
    } // EBeanFactoryImpl()


    public Set<String> getBasePackages() {
        return basePackages;
    }


    public void setBasePackages(Set<String> basePackages) {
        this.basePackages = basePackages;
    }


    public boolean isActivateQueryCaching() {
        return activateQueryCaching;
    }


    public void setActivateQueryCaching(boolean activateQueryCaching) {
        this.activateQueryCaching = activateQueryCaching;
    }


    public void setServerConfig(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }


    protected Object getId(String internalId, Class<? extends Content> kindClass) {
        return internalId;
    } // getId()


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
        if (LOG.isInfoEnabled()) {
            LOG.info("getBean() "+kindClass.getName()+":"+internalId);
        } // if
        return (T) server.find(kindClass, getId(internalId, kindClass));
    } // getBean()


    @Override
    public EContent getBean(String id) {
        return getBean(EContent.class, id);
    } // getBean()


    @Override
    public Class<? extends Content> getBaseClass() {
        return EContent.class;
    } // getBaseClass()


    @Override
    public void beginTransaction() {
        synchronized (server) {
            if (currentTransaction==null) {
                currentTransaction = server.beginTransaction();
            } // if
        } // synchronized
    } // beginTransaction()


    @Override
    public void commitTransaction() {
        synchronized (server) {
            if (currentTransaction!=null) {
                currentTransaction.commit();
                currentTransaction = null;
            } // if
        } // synchronized
    } // commitTransaction()


    @Override
    public void rollbackTransaction() {
        synchronized (server) {
            if (currentTransaction!=null) {
                currentTransaction.rollback();
                currentTransaction = null;
            } // if
        } // synchronized
    } // rollbackTransaction()


    @Override
    protected boolean hasManager() {
        return true;
    } // hasManager()


    @Override
    protected <T extends Content> void apiPersist(T bean) {
        server.save(bean);
    } // apiPersist()


    @Override
    protected <T extends Content> void apiDelete(T bean) {
        server.delete(bean);
    } // apiDelete()


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


    @Override
    @SuppressWarnings("unchecked")
    public <T extends Content> List<T> listBeansOfExactClass(Class<T> cls, String queryString, String orderProperty, Boolean ascending) {
        List<T> result = new ArrayList<T>();
        try {
            String shortTypeName = cls.getSimpleName();
            com.avaje.ebean.Query<T> query = server.find(cls);
            if (orderProperty!=null) {
                String asc = (ascending==Boolean.TRUE) ? " asc" : " desc";
                query = query.orderBy(orderProperty+asc);
            } // if
            // TODO: How to use query string
            // Default is no ordering - not even via IDs
            if (LOG.isInfoEnabled()) {
                LOG.info("listBeansOfExactClass() looking up instances of "+shortTypeName
                        +(queryString==null ? "" : " with condition "+queryString));
            } // if
            List<T> results = query.findList();
            if (LOG.isInfoEnabled()) {
                LOG.info("listBeansOfExactClass() looked up "+results.size()+" raw entries");
            } // if
            filterExactClass(cls, results, result);
            statistics.increase("list beans");
        } catch (Exception e) {
            LOG.error("listBeansOfExactClass() query ", e);
        } // try/catch/finally
        return result;
    } // listBeansOfExactClass()


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
                        for (Class<? extends Content> cls : resolver.getAnnotatedSubclasses(EContent.class, Entity.class)) {
                            if (LOG.isInfoEnabled()) {
                                LOG.info("getAllClasses() * "+cls.getName());
                            } // if
                            classNames.add(cls.getName());
                            tableNameMapping.put(cls.getSimpleName(), cls);
                            allClasses.add(cls);
                        } // for
                        if (LOG.isInfoEnabled()) {
                            LOG.info("getAllClasses() # class names "+classNames.size());
                        } // if
                        startupCache.put(getClassNamesCacheKey(), classNames);
                    } else {
                        // re-fill runtimes caches from persistence startup cache
                        for (String beanClassName : classNames) {
                            Class<? extends Content> cls = (Class<? extends Content>) Class.forName(beanClassName);
                            if (LOG.isInfoEnabled()) {
                                LOG.info("getAllClasses() # "+cls.getName());
                            } // if
                            tableNameMapping.put(cls.getSimpleName(), cls);
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
                Collections.sort(modelClasses, comp);
            } // if
        } // if
        return modelClasses;
    } // getClasses()


    @PostConstruct
    @SuppressWarnings("unchecked")
    public void afterPropertiesSet() {
        final Collection<Class<? extends Content>> classes = getAllClasses();

        for (Class<? extends Content> c : classes) {
            if (LOG.isInfoEnabled()) {
                LOG.info("afterPropertiesSet() class "+c.getName());
            } // if
            serverConfig.addClass(c);
        } // for
        server = EbeanServerFactory.create(serverConfig);
        if (LOG.isInfoEnabled()) {
            LOG.info("afterPropertiesSet() db platform: "+serverConfig.getDatabasePlatform());
            LOG.info("afterPropertiesSet() DDL: "+serverConfig.isDdlGenerate()+"/"+serverConfig.isDdlRun());
        } // if

        Map<String, List<String>> c = startupCache.get(QUERY_CACHE_KEY, queryCache.getClass());
        if (c!=null) {
            queryCache = c;
        } // if
    } // afterPropertiesSet()

} // EBeanFactoryImpl
