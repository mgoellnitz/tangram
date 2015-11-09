/**
 *
 * Copyright 2011-2015 Martin Goellnitz
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import org.tangram.content.BeanFactoryAware;
import org.tangram.content.Content;
import org.tangram.mutable.AbstractMutableBeanFactory;
import org.tangram.util.ClassResolver;
import org.tangram.util.SystemUtils;


public abstract class AbstractJdoBeanFactory extends AbstractMutableBeanFactory implements JdoBeanFactory {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractJdoBeanFactory.class);

    protected PersistenceManagerFactory managerFactory = null;

    protected PersistenceManager manager = null;

    protected List<Class<? extends Content>> allClasses = null;

    private Collection<Class<? extends Content>> additionalClasses = Collections.emptySet();

    private Map<Object, Object> configOverrides = null;

    private boolean prefill = true;

    private String factoryName = "transactions-optional";


    public Map<Object, Object> getConfigOverrides() {
        return configOverrides;
    }


    /**
     *
     * Override Persistence Manager Factory properties given in jdoconfig.xml file.
     * The values added here override and add to the values from jdoconfig.xml.
     * The details of the values are to be taken from the documentation of the JDO implementation.
     *
     * @param configOverrides name value pair mapping with values for the given JDO implementation.
     */
    public void setConfigOverrides(Map<Object, Object> configOverrides) {
        this.configOverrides = configOverrides;
    }


    public boolean isPrefill() {
        return prefill;
    }


    public void setPrefill(boolean prefill) {
        this.prefill = prefill;
    }


    /**
     * Override the default id when obtaining the persistence factory.
     *
     * @param factoryName persistence factory name to be used subsequently.
     */
    public void setFactoryName(String factoryName) {
        this.factoryName = factoryName;
    }


    protected abstract Object getObjectId(String internalId, Class<? extends Content> kindClass, String kind);


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
        Object oid = getObjectId(internalId, kindClass, kind);
        LOG.debug("getBean() {} {} oid={}", kindClass.getName(), internalId, oid);
        return convert(cls, manager.getObjectById(kindClass, oid));
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
        LOG.debug("createBean() beginning transaction");
        beginTransaction();

        LOG.debug("createBean() creating new instance of {}", cls.getName());
        T bean = manager.newInstance(cls);

        statistics.increase("create bean");
        return bean;
    } // createBean()


    @Override
    public <T extends Content> List<T> listBeansOfExactClass(Class<T> cls, String queryString, String orderProperty, Boolean ascending) {
        List<T> result = new ArrayList<>();
        try {
            Extent<T> extent = manager.getExtent(cls, false);
            Query query = queryString==null ? manager.newQuery(extent) : manager.newQuery(extent, queryString);
            // Default is no ordering - not even via IDs
            if (orderProperty!=null) {
                String order = orderProperty+(ascending ? " asc" : " desc");
                query.setOrdering(order);
            } // if
            // Will be extended once we decide to introduce start/end
            // if (end!=null) {
            // long from = start!=null ? start : 0;
            // query.setRange(from, end+1);
            // } // if
            LOG.info("listBeansOfExactClass() looking up instances of {} {}", cls.getSimpleName(), (queryString==null ? "-" : " with condition "+queryString));
            List<T> results = SystemUtils.convert(query.execute());
            LOG.info("listBeansOfExactClass() looked up {} raw entries", results.size());
            for (T o : results) {
                if (o instanceof BeanFactoryAware) {
                    ((BeanFactoryAware) o).setBeanFactory(this);
                } // if
                result.add(o);
            } // for
            statistics.increase("list beans");
        } catch (Exception e) {
            LOG.error("listBeansOfExactClass() query ", e);
        } // try/catch/finally
        return result;
    } // listBeansOfExactClass()


    /**
     * Get a collection of model related classes.
     *
     * This method also returns the abstract classes or interfaces in the base packages.
     *
     * @return collection with all classes
     */
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
                        resolvedClasses.addAll(resolver.getAnnotatedSubclasses(getBaseClass(), PersistenceCapable.class));
                        resolvedClasses.addAll(resolver.getSubclasses(Content.class));
                        for (Class<? extends Content> cls : resolvedClasses) {
                            LOG.info("getAllClasses() * {}", cls.getName());
                            if (!allClasses.contains(cls)) {
                                classNames.add(cls.getName());
                                allClasses.add(cls);
                            } // if
                        } // for
                        LOG.info("getAllClasses() * class names {}", classNames.size());
                        startupCache.put(getClassNamesCacheKey(), classNames);
                    } else {
                        for (String beanClassName : classNames) {
                            Class<? extends Content> cls = ClassResolver.loadClass(beanClassName);
                            LOG.info("getAllClasses() # {}", cls.getName());
                            allClasses.add(cls);
                        } // for
                    } // if
                    allClasses.addAll(additionalClasses);
                } catch (Exception e) {
                    LOG.error("getAllClasses() outer", e);
                } // try/catch
            } // if
        } // synchronized
        return allClasses;
    } // getAllClasses()


    @Override
    public void setAdditionalClasses(Collection<Class<? extends Content>> classes) {
        Set<Class<? extends Content>> classSet = new HashSet<>();
        if (classes!=null) {
            for (Class<? extends Content> cls : classes) {
                if ((JdoContent.class.isAssignableFrom(cls))&&(cls.getAnnotation(PersistenceCapable.class)!=null)) {
                    classSet.add(cls);
                } // if
            } // for
        } // if
        additionalClasses = classSet;
        synchronized (this) {
            allClasses = null;
            modelClasses = null;
        } // synchronized
    } // setAdditionalClasses()


    protected Map<? extends Object, ? extends Object> getFactoryConfigOverrides() {
        return getConfigOverrides()==null ? Collections.emptyMap() : getConfigOverrides();
    } // getFactoryConfigOverrides()


    protected void appendItem(StringBuffer result, String filterProperty, String filterValue) {
        result.append('(');
        result.append(filterProperty);
        result.append(".indexOf(\"");
        result.append(filterValue);
        result.append("\") >= 0)");
    } // appendItem()


    @Override
    public Object getManager() {
        return manager;
    } // getManager()


    @PostConstruct
    public void afterPropertiesSet() {
        Map<? extends Object, ? extends Object> overrides = getFactoryConfigOverrides();
        LOG.info("afterPropertiesSet() using overrides for persistence manager factory: {}", overrides);
        managerFactory = JDOHelper.getPersistenceManagerFactory(overrides, factoryName);
        manager = managerFactory.getPersistenceManager();

        // Just to prefill
        if (prefill) {
            Collection<Class<? extends Content>> theClasses = getClasses();
            LOG.info("afterPropertiesSet() prefilling done for {}: {}", getBasePackages(), theClasses);
        } // if
        Map<String, List<String>> c = SystemUtils.convert(startupCache.get(QUERY_CACHE_KEY, queryCache.getClass()));
        if (c!=null) {
            queryCache = c;
        } // if
    } // afterPropertiesSet()

} // AbstractJdoBeanFactory
