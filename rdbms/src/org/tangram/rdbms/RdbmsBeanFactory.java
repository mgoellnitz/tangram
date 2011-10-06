/**
 * 
 * Copyright 2011 Martin Goellnitz
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
package org.tangram.rdbms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.datanucleus.identity.OID;
import org.datanucleus.identity.OIDImpl;
import org.tangram.content.Content;
import org.tangram.jdo.AbstractJdoBeanFactory;
import org.tangram.jdo.JdoContent;

public class RdbmsBeanFactory extends AbstractJdoBeanFactory {

    private static final Log log = LogFactory.getLog(RdbmsBeanFactory.class);


    @Override
    @SuppressWarnings("unchecked")
    public <T extends Content> T getBean(Class<T> cls, String id) {
        if (activateCaching&&(cache.containsKey(id))) {
            statistics.increase("get bean cached");
            return (T)cache.get(id);
        } // if
        T result = null;
        try {
            if (modelClasses==null) {
                getClasses();
            } // if
            String kind = null;
            Long numericId = null;
            int idx = id.indexOf(':');
            if (idx>0) {
                kind = id.substring(0, idx);
                numericId = Long.parseLong(id.substring(idx+1));
            } // if
            Class<? extends Content> kindClass = tableNameMapping.get(kind);
            if (kindClass==null) {
                throw new Exception("Passed over kind "+kind+" not valid");
            } // if
            if ( !(cls.isAssignableFrom(kindClass))) {
                throw new Exception("Passed over class "+cls.getSimpleName()+" does not match "
                        +kindClass.getSimpleName());
            } // if
            OID oid = new OIDImpl(kindClass.getName(), numericId);
            if (log.isWarnEnabled()) {
                log.warn("getBean() "+kindClass.getName()+" "+numericId+" "+oid);
            } // if
            result = (T)manager.getObjectById(oid);
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
        if (id instanceof OID) {
            OID oid = (OID)id;
            String pcClass = oid.getPcClass();
            int idx = pcClass.lastIndexOf('.');
            pcClass = pcClass.substring(idx+1);
            return pcClass+":"+oid.getKeyValue();
        } else {
            return ""+id;
        } // if
    } // postprocessPlainId()

} // DatanucleusBeanFactory
