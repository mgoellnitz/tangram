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
package org.tangram.gae;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheFactory;
import net.sf.jsr107cache.CacheManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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


    @Override
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
                throw new Exception("Passed over class "+cls.getSimpleName()+" does not match "+kindClass.getSimpleName());
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
    public Collection<Class<? extends Content>> getAllClasses() {
        synchronized (this) {
            if (allClasses==null) {
                try {
                    CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
                    Cache jsrCache = cacheFactory.createCache(Collections.emptyMap());

                    String cacheKey = com.google.appengine.api.utils.SystemProperty.applicationVersion.get();
                    Object co = jsrCache.get(cacheKey);
                    if (co!=null) {
                        if (co instanceof List) {
                            allClasses = new ArrayList<Class<? extends Content>>();
                            tableNameMapping = new HashMap<String, Class<? extends Content>>();
                            List<String> classNames = (List<String>)co;
                            for (String beanClassName : classNames) {
                                Class<? extends Content> cls = (Class<? extends Content>)Class.forName(beanClassName);
                                if (log.isInfoEnabled()) {
                                    log.info("getAllClasses() # "+cls.getName());
                                } // if
                                tableNameMapping.put(cls.getSimpleName(), cls);
                                allClasses.add(cls);
                            } // for
                        } // if
                    } else {
                        super.getAllClasses();
                        List<String> classNames = new ArrayList<String>();
                        for (Class<?> cls : allClasses) {
                            classNames.add(cls.getName());
                        } // for
                        jsrCache.put(cacheKey, classNames);
                    } // if
                } catch (Exception e) {
                    log.error("getAllClasses() cached gae wrapper", e);
                } // try/catch
            } // if
        } // synchronized
        return allClasses;
    } // getAllClasses()

} // GaeBeanFactory