/**
 *
 * Copyright 2013-2017 Martin Goellnitz
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

import groovy.lang.Singleton;
import io.ebean.EbeanServer;
import io.ebean.EbeanServerFactory;
import io.ebean.Query;
import io.ebean.Transaction;
import io.ebean.config.ServerConfig;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.persistence.Entity;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.content.Content;
import org.tangram.mutable.AbstractMutableBeanFactory;
import org.tangram.mutable.MutableBeanFactory;
import org.tangram.util.ClassResolver;
import org.tangram.util.SystemUtils;


@Named("beanFactory")
@Singleton
public class EBeanFactoryImpl extends AbstractMutableBeanFactory<EbeanServer, Query<?>> implements MutableBeanFactory<EbeanServer, Query<?>> {

    private static final Logger LOG = LoggerFactory.getLogger(EBeanFactoryImpl.class);

    private ServerConfig serverConfig;

    private EbeanServer server;

    private Transaction currentTransaction = null;

    protected List<Class<? extends Content>> allClasses = null;


    public void setServerConfig(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }


    protected Object getId(String internalId, Class<? extends Content> kindClass) {
        return internalId;
    } // getId()


    @Override
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
        LOG.info("getBean() {}:{}", kindClass.getName(), internalId);
        return convert(cls, server.find(kindClass, getId(internalId, kindClass)));
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


    @Override
    public Query<? extends Content> createQuery(Class<? extends Content> c, String expression) {
        return StringUtils.isEmpty(expression) ? server.createQuery(c) : server.createQuery(c, expression);
    } // createQuery()


    @Override
    public <T extends Content> List<T> listBeans(Class<T> cls, String query, String orderProperty, Boolean ascending) {
        List<T> results = new ArrayList<>();
        try {
            for (Class<T> c : getImplementingBaseClasses(cls)) {
                String shortTypeName = cls.getSimpleName();
                LOG.info("listBeans() ebean source query is {} ", query);
                Query<T> q = StringUtils.isEmpty(query) ? server.createQuery(c) : server.createQuery(c, "where "+query);
                if (orderProperty!=null) {
                    String asc = (ascending) ? " asc" : " desc";
                    q = q.orderBy(orderProperty+asc);
                } // if
                // Default is no ordering - not even via IDs
                LOG.info("listBeans() looking up instances of {}{}", shortTypeName, (q==null ? "" : " with condition "+q));
                LOG.info("listBeans() ebean query is {} ", q.getGeneratedSql());
                results.addAll(SystemUtils.convert(q.findList()));
                injectBeanFactory(results);
            } // for
            LOG.info("listBeans() looked up {} raw entries", results.size());
            statistics.increase("list beans");
        } catch (Exception e) {
            LOG.error("listBeans() query ", e);
        } // try/catch/finally
        return results;
    } // listBeans()


    @Override
    public <T extends Content> List<T> listBeans(Query<?> query) {
        List<T> result = new ArrayList<>();
        try {
            String shortTypeName = query.getBeanType().getSimpleName();
            // Default is no ordering - not even via IDs
            LOG.info("listBeans() looking up instances of {}{}", shortTypeName, (query==null ? "" : " with condition "+query));
            LOG.info("listBeans() ebean query is {} ", query.getGeneratedSql());
            result.addAll(SystemUtils.convert(query.findList()));
            injectBeanFactory(result);
            LOG.info("listBeans() looked up {} raw entries", result.size());
            statistics.increase("list beans");
        } catch (Exception e) {
            LOG.error("listBeans() query ", e);
        } // try/catch/finally
        return result;
    } // listBeans()


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
                        resolvedClasses.addAll(resolver.getAnnotatedSubclasses(EContent.class, Entity.class));
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
                            Class<? extends Content> cls = SystemUtils.convert(Class.forName(beanClassName));
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


    @Override
    public EbeanServer getManager() {
        return server;
    } // getManager()


    @PostConstruct
    public void afterPropertiesSet() {
        LOG.debug("afterPropertiesSet()");
        for (Class<? extends Content> c : getAllClasses()) {
            LOG.info("afterPropertiesSet() class {}", c.getName());
            serverConfig.addClass(c);
        } // for
        server = EbeanServerFactory.create(serverConfig);
        LOG.info("afterPropertiesSet() db platform: {}", serverConfig.getDatabasePlatform());
        LOG.info("afterPropertiesSet() DDL: {}/{}", serverConfig.isDdlGenerate(), serverConfig.isDdlRun());

        Map<String, List<String>> c = SystemUtils.convert(startupCache.get(QUERY_CACHE_KEY, queryCache.getClass()));
        if (c!=null) {
            queryCache = c;
        } // if
    } // afterPropertiesSet()

} // EBeanFactoryImpl
