/**
 * 
 * Copyright 2011-2012 Martin Goellnitz
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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

import javax.jdo.Extent;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.tangram.content.AbstractBeanFactory;
import org.tangram.content.BeanListener;
import org.tangram.content.Content;
import org.tangram.monitor.Statistics;

public abstract class AbstractJdoBeanFactory extends AbstractBeanFactory implements JdoBeanFactory, InitializingBean {

    private static final Log log = LogFactory.getLog(AbstractJdoBeanFactory.class);

    @Autowired
    protected Statistics statistics;

    public static final PersistenceManagerFactory pmfInstance = JDOHelper.getPersistenceManagerFactory("transactions-optional");

    protected PersistenceManager manager = pmfInstance.getPersistenceManager();

    protected List<Class<? extends Content>> modelClasses = null;

    protected List<Class<? extends Content>> allClasses = null;

    protected Map<String, Class<? extends Content>> tableNameMapping = null;

    protected Map<Class<? extends Content>, List<Class<? extends Content>>> implementingClassesMap = null;

    protected Map<String, Content> cache = new HashMap<String, Content>();

    protected boolean activateCaching = false;

    private Set<String> basePackages;

    private boolean activateQueryCaching = false;

    private boolean prefill = true;

    private Map<Class<? extends Content>, List<BeanListener>> attachedListeners = new HashMap<Class<? extends Content>, List<BeanListener>>();

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


    public boolean isPrefill() {
        return prefill;
    }


    public void setPrefill(boolean prefill) {
        this.prefill = prefill;
    }


    @Override
    public JdoContent getBean(String id) {
        return getBean(JdoContent.class, id);
    } // getBean()


    @Override
    public <T extends Content> T getBeanForUpdate(Class<T> cls, String id) {
        T bean = getBean(cls, id);
        manager.currentTransaction().begin();
        return bean;
    } // getBeanForUpdate()


    @Override
    public JdoContent getBeanForUpdate(String id) {
        return getBeanForUpdate(JdoContent.class, id);
    } // getBeanForUpdate()


    /**
     * remember that the newly created bean has to be persisted in the now open transaction!
     */
    @Override
    public <T extends Content> T createBean(Class<T> cls) {
        if (log.isDebugEnabled()) {
            log.debug("createBean() obtaining persistence manager");
        } // if
        try {
            manager.currentTransaction().begin();
            if (log.isDebugEnabled()) {
                log.debug("createBean() creating new instance of "+cls.getName());
            } // if
            T bean = manager.newInstance(cls);
            if (log.isDebugEnabled()) {
                log.debug("createBean() populating new instance");
            } // if
            bean.setBeanFactory(this);
            ((JdoContent)bean).setManager(manager);

            statistics.increase("create bean");
            return bean;
        } finally {
        } // try/catch/finally
    } // createBean()


    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <T extends Content> List<T> listBeansOfExactClass(Class<T> cls, String queryString, String orderProperty, Boolean ascending) {
        List<T> result = new ArrayList<T>();
        try {
            Extent extent = manager.getExtent(cls, false);
            Query query = queryString==null ? manager.newQuery(extent) : manager.newQuery(extent, queryString);
            // Default is no ordering - not even via IDs
            if (orderProperty!=null) {
                String asc = " asc";
                if (ascending!=null) {
                    asc = ascending ? " asc" : " desc";
                } // if
                String order = orderProperty+asc;
                query.setOrdering(order);
            } // if
              // TOOD: will be extended once we decide to introduce start/end
              // if (end!=null) {
              // long from = start!=null ? start : 0;
              // query.setRange(from, end+1);
              // } // if
            if (log.isInfoEnabled()) {
                log.info("listBeansOfExactClass() looking up instances of "+cls.getSimpleName()
                        +(queryString==null ? "" : " with condition "+queryString));
            } // if
            List<Object> results = (List<Object>)query.execute();
            if (log.isInfoEnabled()) {
                log.info("listBeansOfExactClass() looked up "+results.size()+" raw entries");
            } // if
            for (Object o : results) {
                if (o instanceof Content) {
                    JdoContent c = (JdoContent)o;
                    c.setBeanFactory(this);
                    c.setManager(manager);

                    result.add((T)c);
                } // if
            } // for
            statistics.increase("list beans");
        } catch (Exception e) {
            log.error("listBeansOfExactClass() query ", e);
        } // try/catch/finally
        return result;
    } // listBeansOfExactClass()


    private <T> String getCacheKey(Class<T> cls, String queryString, String orderProperty) {
        return cls.getName()+":"+orderProperty+":"+queryString;
    } // getCacheKey()


    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <T extends Content> List<T> listBeans(Class<T> cls, String queryString, String orderProperty, Boolean ascending) {
        List<T> result = null;
        if (log.isInfoEnabled()) {
            log.info("listBeans() looking up instances of "+cls.getSimpleName()
                    +(queryString==null ? "" : " with condition "+queryString));
        } // if
        String key = null;
        if (activateQueryCaching) {
            key = getCacheKey(cls, queryString, orderProperty);
            List<String> idList = queryCache.get(key);
            if (idList!=null) {
                result = new ArrayList<T>(idList.size());
                for (String id : idList) {
                    result.add(getBean(cls, id));
                } // for
                statistics.increase("query beans cached");
            } // if
        } // if
        if (result==null) {
            result = new ArrayList<T>();
            for (Class c : getClasses()) {
                if (cls.isAssignableFrom(c)) {
                    result.addAll(listBeansOfExactClass(c, queryString, orderProperty, ascending));
                } // if
            } // for
            if (activateQueryCaching) {
                List<String> idList = new ArrayList<String>(result.size());
                for (T content : result) {
                    idList.add(content.getId());
                } // for
                queryCache.put(key, idList);
            } // if
            statistics.increase("query beans uncached");
        } // if
        if (log.isInfoEnabled()) {
            log.info("listBeans() looked up "+result.size()+" raw entries");
        } // if
        return result;
    } // listBeans()


    @SuppressWarnings("unchecked")
    private <T extends Content> Class<T> getKeyClass(String key) {
        String className = key.split(":")[0];
        try {
            return (Class<T>)Class.forName(className);
        } catch (ClassNotFoundException cnfe) {
            return null;
        } // try/catch
    } // getClass()


    @Override
    public void clearCacheFor(Class<? extends Content> cls) {
        statistics.increase("bean cache clear");
        cache.clear();
        if (log.isWarnEnabled()) {
            log.warn("clearCache() for "+cls.getSimpleName());
        } // if
        try {
            for (Class<? extends Content> c : attachedListeners.keySet()) {
                boolean assignableFrom = c.isAssignableFrom(cls);
                if (log.isInfoEnabled()) {
                    log.info("clearCache() "+c.getSimpleName()+"? "+assignableFrom);
                } // if
                if (assignableFrom) {
                    List<BeanListener> listeners = attachedListeners.get(c);
                    if (log.isInfoEnabled()) {
                        log.info("clearCache() triggering "+(listeners==null ? "no" : listeners.size())+" listeners");
                    } // if
                    if (listeners!=null) {
                        for (BeanListener listener : listeners) {
                            listener.reset();
                        } // for
                    } // if
                } // if
            } // for

            Collection<String> removeKeys = new HashSet<String>();
            for (Object keyObject : queryCache.keySet()) {
                String key = (String)keyObject;
                Class<? extends Content> c = getKeyClass(key);
                boolean assignableFrom = c.isAssignableFrom(cls);
                if (log.isInfoEnabled()) {
                    log.info("clearCache("+key+") "+c.getSimpleName()+"? "+assignableFrom);
                } // if
                if (assignableFrom) {
                    removeKeys.add(key);
                } // if
            } // for
            for (String key : removeKeys) {
                queryCache.remove(key);
            } // for
        } catch (Exception e) {
            log.error("clearCache() for "+cls.getSimpleName(), e);
        } // try/catch
    } // clearCacheFor()


    @Override
    public void addListener(Class<? extends Content> cls, BeanListener listener) {
        synchronized (attachedListeners) {
            List<BeanListener> listeners = attachedListeners.get(cls);
            if (listeners==null) {
                listeners = new ArrayList<BeanListener>();
                attachedListeners.put(cls, listeners);
            } // if
            listeners.add(listener);
        } // sync
        if (log.isInfoEnabled()) {
            log.info("addListener() "+cls.getSimpleName()+": "+attachedListeners.get(cls).size());
        } // if
    } // addListener()


    @Override
    @SuppressWarnings("unchecked")
    public Collection<Class<? extends Content>> getAllClasses() {
        synchronized (this) {
            if (allClasses==null) {
                allClasses = new ArrayList<Class<? extends Content>>();
                tableNameMapping = new HashMap<String, Class<? extends Content>>();

                try {
                    /*
                     * new: we also want abstract classes and interfaces here, so override "isCandidateComponent()" to
                     * leave out beanDefinition.getMetadata().isConcrete()
                     */
                    ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(true) {
                        @Override
                        protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                            return (beanDefinition.getMetadata().isIndependent());
                        }
                    };
                    provider.addIncludeFilter(new AssignableTypeFilter(JdoContent.class));

                    // scan
                    Set<BeanDefinition> components = new HashSet<BeanDefinition>();
                    for (String pack : basePackages) {
                        try {
                            if (log.isInfoEnabled()) {
                                log.info("getAllClasses() "+pack+" "+components.size());
                            } // if
                            components.addAll(provider.findCandidateComponents(pack));
                        } catch (Exception e) {
                            log.error("getAllClasses() inner "+e.getMessage());
                        } // try/catch
                    } // for
                    if (log.isInfoEnabled()) {
                        log.info("getAllClasses() size()="+components.size());
                    } // if
                    for (BeanDefinition component : components) {
                        try {
                            String beanClassName = component.getBeanClassName();
                            if (log.isDebugEnabled()) {
                                log.debug("getAllClasses() component.getBeanClassName()="+beanClassName);
                            } // if
                            Class<? extends Content> cls = (Class<? extends Content>)Class.forName(beanClassName);
                            if (JdoContent.class.isAssignableFrom(cls)) {
                                if (log.isInfoEnabled()) {
                                    log.info("getAllClasses() * "+cls.getName());
                                } // if
                                tableNameMapping.put(cls.getSimpleName(), cls);
                                allClasses.add(cls);
                            } else {
                                if (log.isDebugEnabled()) {
                                    log.debug("getAllClasses() "+cls.getName());
                                } // if
                            } // if
                        } catch (Exception e) {
                            log.error("getAllClasses() inner", e);
                        } // try/catch
                    } // for
                } catch (Exception e) {
                    log.error("getAllClasses() outer", e);
                } // try/catch
            } // if
        } // if
        return allClasses;
    } // getAllClasses()


    @Override
    public Collection<Class<? extends Content>> getClasses() {
        synchronized (this) {
            if (modelClasses==null) {
                modelClasses = new ArrayList<Class<? extends Content>>();
                for (Class<? extends Content> cls : getAllClasses()) {
                    if ( !((cls.getModifiers()&Modifier.ABSTRACT)==Modifier.ABSTRACT)) {
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


    private List<Class<? extends Content>> getImplementingClasses(Class<? extends Content> baseClass) {
        List<Class<? extends Content>> result = new ArrayList<Class<? extends Content>>();

        for (Class<? extends Content> c : getClasses()) {
            if ((c.getModifiers()&Modifier.ABSTRACT)==0) {
                if (baseClass.isAssignableFrom(c)) {
                    result.add(c);
                } // if
            } // if
        } // for

        return result;
    } // getImplementingClasses()


    /**
     * just to support JSP weak calling of methods with no parameters
     * 
     * @param baseClass
     * @return
     */
    @Override
    public Map<Class<? extends Content>, List<Class<? extends Content>>> getImplementingClassesMap() {
        if (implementingClassesMap==null) {
            implementingClassesMap = new HashMap<Class<? extends Content>, List<Class<? extends Content>>>();

            // Add the very basic root classes directly here - they won't get auto detected otherwise
            implementingClassesMap.put(JdoContent.class, getImplementingClasses(JdoContent.class));
            implementingClassesMap.put(Content.class, getImplementingClasses(Content.class));
            for (Class<? extends Content> c : getAllClasses()) {
                implementingClassesMap.put(c, getImplementingClasses(c));
            } // for
        } // if
        return implementingClassesMap;
    } // getImplementingClassMap()


    @Override
    public void afterPropertiesSet() throws Exception {
        // Just to prefill
        if (prefill) {
            getClasses();
        } // if
    } // afterPropertiesSet()

} // AbstractJdoBeanFactory
