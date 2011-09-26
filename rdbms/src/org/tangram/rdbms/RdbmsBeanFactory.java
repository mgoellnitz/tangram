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
