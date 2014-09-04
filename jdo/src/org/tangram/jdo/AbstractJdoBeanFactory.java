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
     * Override Persistence Manager Factory properties given in jdoconfig.xml
     *
     * @param configOverrides
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
     * @param factoryName
     */
    public void setFactoryName(String factoryName) {
        this.factoryName = factoryName;
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("getBean() "+kindClass.getName()+" "+internalId+" oid="+oid);
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
                LOG.info("listBeansOfExactClass() looking up instances of "+cls.getSimpleName()+(queryString==null ? "" : " with condition "+queryString));
            } // if
            List<T> results = (List<T>) query.execute();
            if (LOG.isInfoEnabled()) {
                LOG.info("listBeansOfExactClass() looked up "+results.size()+" raw entries");
            } // if
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
    @SuppressWarnings("unchecked")
    public Collection<Class<? extends Content>> getAllClasses() {
        synchronized (this) {
            if (allClasses==null) {
                allClasses = new ArrayList<Class<? extends Content>>();
                try {
                    List<String> classNames = startupCache.get(getClassNamesCacheKey(), List.class);
                    if (classNames==null) {
                        ClassResolver resolver = new ClassResolver(getBasePackages());
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
        Set<Class<? extends Content>> classSet = new HashSet<Class<? extends Content>>();
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


    @PostConstruct
    @SuppressWarnings("unchecked")
    public void afterPropertiesSet() {
        Map<? extends Object, ? extends Object> overrides = getFactoryConfigOverrides();
        if (LOG.isInfoEnabled()) {
            LOG.info("afterPropertiesSet() using overrides for persistence manager factory: "+overrides);
        } // if
        managerFactory = JDOHelper.getPersistenceManagerFactory(overrides, factoryName);
        manager = managerFactory.getPersistenceManager();

        // Just to prefill
        if (prefill) {
            final Collection<Class<? extends Content>> theClasses = getClasses();
            if (LOG.isInfoEnabled()) {
                LOG.info("afterPropertiesSet() prefilling done for "+getBasePackages()+": "+theClasses);
            } // if
        } // if
        Map<String, List<String>> c = startupCache.get(QUERY_CACHE_KEY, queryCache.getClass());
        if (c!=null) {
            queryCache = c;
        } // if
    } // afterPropertiesSet()

} // AbstractJdoBeanFactory
