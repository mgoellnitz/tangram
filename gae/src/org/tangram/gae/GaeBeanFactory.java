package org.tangram.gae;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheFactory;
import net.sf.jsr107cache.CacheManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.tangram.content.Content;
import org.tangram.jdo.AbstractJdoBeanFactory;
import org.tangram.jdo.JdoContent;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.repackaged.com.google.common.util.Base64;

public class GaeBeanFactory extends AbstractJdoBeanFactory {

    private static final Log log = LogFactory.getLog(GaeBeanFactory.class);

    private boolean encodeIds = true;

    public boolean isEncodeIds() {
        return encodeIds;
    }


    public void setEncodeIds(boolean encodeIds) {
        this.encodeIds = encodeIds;
    }


    @SuppressWarnings("unchecked")
    public <T extends Content> T getBean(Class<T> cls, String id) {
        if (activateCaching&&(cache.containsKey(id))) {
            statistics.increase("get bean cached");
            return (T)cache.get(id);
        } // if
        T result = null;
        try {
            Key key = null;
            String kind = null;
            long numericId = 0;
            int idx = id.indexOf(':');
            if (idx>0) {
                kind = id.substring(0, idx);
                numericId = Long.parseLong(id.substring(idx+1));
            } else {
                if (id.length()>25) {
                    Key k = KeyFactory.stringToKey(id);
                    kind = k.getKind();
                    numericId = k.getId();
                } else {
                    byte[] idBytes = Base64.decodeWebSafe(id);
                    String idString = new String(idBytes, "UTF-8");
                    idx = idString.indexOf(':');
                    if (idx>0) {
                        kind = idString.substring(0, idx);
                        numericId = Long.parseLong(idString.substring(idx+1));
                    } // if
                } // if
            } // if
            if (log.isDebugEnabled()) {
                log.debug("getBean() kind="+kind);
                log.debug("getBean() numericId="+numericId);
            } // if
            key = KeyFactory.createKey(kind, numericId);
            if (modelClasses==null) {
                getClasses();
            } // if
            Class<? extends Content> kindClass = tableNameMapping.get(kind);
            if ( !(cls.isAssignableFrom(kindClass))) {
                throw new Exception("Passed over class "+cls.getSimpleName()+" does not match "
                        +kindClass.getSimpleName());
            } // if
            result = (T)manager.getObjectById(kindClass, key);
            ((JdoContent)result).setManager(manager);
            result.setBeanFactory(this);

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
    public String postprocessPlainId(Object id) {
        String result = ""+id;
        try {
            Key key = KeyFactory.stringToKey(result);
            result = key.getKind()+":"+key.getId();
            if (encodeIds) {
                try {
                    result = Base64.encodeWebSafe(result.getBytes("UTF-8"), true);
                } catch (Exception e) {
                    log.warn("postprocessPlainId() "+e.getLocalizedMessage());
                } // try/catch
            } // if
        } catch (Exception e) {
            // never mind
        } // try/catch
        return result;
    } // postprocessPlainId()


    @SuppressWarnings("unchecked")
    @Override
    public Collection<Class<? extends Content>> getClasses() {
        synchronized (this) {
            if (modelClasses==null) {
                modelClasses = new ArrayList<Class<? extends Content>>();
                tableNameMapping = new HashMap<String, Class<? extends Content>>();

                try {
                    CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
                    Cache cache = cacheFactory.createCache(Collections.emptyMap());

                    String cacheKey = com.google.appengine.api.utils.SystemProperty.applicationVersion.get();
                    Object co = cache.get(cacheKey);
                    if (co!=null) {
                        if (co instanceof List) {
                            List<String> classNames = (List<String>)co;
                            for (String beanClassName : classNames) {
                                Class<? extends Content> cls = (Class<? extends Content>)Class.forName(beanClassName);
                                if (log.isInfoEnabled()) {
                                    log.info("getClasses() # "+cls.getName());
                                } // if
                                tableNameMapping.put(cls.getSimpleName(), cls);
                                modelClasses.add(cls);
                            } // for
                        } // if
                    } else {
                        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(
                                true);
                        provider.addIncludeFilter(new AssignableTypeFilter(JdoContent.class));

                        // scan
                        Set<BeanDefinition> components = new HashSet<BeanDefinition>();
                        for (String pack : getBasePackages()) {
                            try {
                                if (log.isInfoEnabled()) {
                                    log.info("getClasses() "+pack+" "+components.size());
                                } // if
                                components.addAll(provider.findCandidateComponents(pack));
                            } catch (Exception e) {
                                log.error("getClasses() inner "+e.getMessage());
                            } // try/catch
                        } // for
                        if (log.isInfoEnabled()) {
                            log.info("getClasses() size()="+components.size());
                        } // if
                        for (BeanDefinition component : components) {
                            try {
                                String beanClassName = component.getBeanClassName();
                                if (log.isDebugEnabled()) {
                                    log.debug("getClasses() component.getBeanClassName()="+beanClassName);
                                } // if
                                Class<? extends Content> cls = (Class<? extends Content>)Class.forName(beanClassName);
                                if ( !((cls.getModifiers()&Modifier.ABSTRACT)==Modifier.ABSTRACT)) {
                                    if (JdoContent.class.isAssignableFrom(cls)) {
                                        if (log.isInfoEnabled()) {
                                            log.info("getClasses() * "+cls.getName());
                                        } // if
                                        tableNameMapping.put(cls.getSimpleName(), cls);
                                        modelClasses.add(cls);
                                    } else {
                                        if (log.isDebugEnabled()) {
                                            log.debug("getClasses() "+cls.getName());
                                        } // if
                                    } // if
                                } // if
                            } catch (Exception e) {
                                log.error("getClasses() inner", e);
                            } // try/catch
                        } // for
                        Comparator<Class<?>> comp = new Comparator<Class<?>>() {

                            @Override
                            public int compare(Class<?> o1, Class<?> o2) {
                                return o1.getName().compareTo(o2.getName());
                            } // compareTo()

                        };
                        Collections.sort(modelClasses, comp);

                        List<String> classNames = new ArrayList<String>();
                        for (Class<?> cls : modelClasses) {
                            classNames.add(cls.getName());
                        } // for
                        cache.put(cacheKey, classNames);
                    } // if
                } catch (Exception e) {
                    log.error("getClasses() outer", e);
                } // try/catch
            } // if
        } // if
        return modelClasses;
    } // getClasses()


} // GaeBeanFactory