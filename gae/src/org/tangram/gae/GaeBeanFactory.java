/**
 * 
 * Copyright 2011-2013 Martin Goellnitz
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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jmx.access.InvocationFailureException;
import org.tangram.content.Content;
import org.tangram.jdo.AbstractJdoBeanFactory;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class GaeBeanFactory extends AbstractJdoBeanFactory {

    private static final Log log = LogFactory.getLog(GaeBeanFactory.class);

    private boolean useHdrDatastore = true;


    public boolean isUseHdrDatastore() {
        return useHdrDatastore;
    }


    public void setUseHdrDatastore(boolean useHdrDatastore) {
        this.useHdrDatastore = useHdrDatastore;
    }


    /**
     * set cross group transactions only to true on HDR data stores. So projects which actually don't use it can still
     * run in the same setup.
     */
    @Override
    protected Map<? extends Object, ? extends Object> getFactoryConfigOverrides() {
        Map<Object, Object> result = new HashMap<Object, Object>();
        if (isUseHdrDatastore()) {
            result.put("datanucleus.appengine.datastoreEnableXGTransactions", Boolean.TRUE);
        } // if
        return result;
    } // getFactoryConfigOverrides()


    /**
     * we had to override the whole getBeans, so this one should never be called.
     */
    @Override
    protected Object getObjectId(String internalId, Class<? extends Content> kindClass) {
        throw new InvocationFailureException("GAE implementation is somewhat different");
    } // getObjectId()


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
    protected String getClassNamesCacheKey() {
        return com.google.appengine.api.utils.SystemProperty.applicationVersion.get();
    } // getClassNamesCacheKey()

} // GaeBeanFactory