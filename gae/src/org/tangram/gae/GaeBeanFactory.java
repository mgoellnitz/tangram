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
package org.tangram.gae;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Named;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.content.Content;
import org.tangram.jdo.AbstractJdoBeanFactory;
import org.tangram.jdo.JdoContent;


@Named("beanFactory")
@Singleton
public class GaeBeanFactory extends AbstractJdoBeanFactory {

    private static final Logger LOG = LoggerFactory.getLogger(GaeBeanFactory.class);

    private boolean useHdrDatastore = true;


    public boolean isUseHdrDatastore() {
        return useHdrDatastore;
    }


    public void setUseHdrDatastore(boolean useHdrDatastore) {
        this.useHdrDatastore = useHdrDatastore;
    }


    @Override
    public Class<? extends Content> getBaseClass() {
        return GaeContent.class;
    } // getBaseClass()


    /**
     * set cross group transactions only to true on HDR data stores.
     *
     * So projects which actually don't use it can still run in the same setup.
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
    protected Object getObjectId(String internalId, Class<? extends Content> kindClass, String kind) {
        long numericId = Long.parseLong(internalId);
        if (LOG.isDebugEnabled()) {
            LOG.debug("getObjectId() kind="+kind);
            LOG.debug("getObjectId() numericId="+numericId);
        } // if
        return KeyFactory.createKey(kind, numericId);
    } // getObjectId()


    /**
     * TODO: This whole method is only needed for very old repositories with verbatim ID-Strings instead of references.
     */
    @Override
    public JdoContent getBean(String id) {
        if (id==null) {
            return null;
        } // if
        if (id.indexOf(':')<0) {
            Key key = KeyFactory.stringToKey(id);
            id = key.getKind()+":"+key.getId();
        } // if
        return super.getBean(id);
    } // getBean()


    @Override
    protected String getClassNamesCacheKey() {
        return com.google.appengine.api.utils.SystemProperty.applicationVersion.get();
    } // getClassNamesCacheKey()

} // GaeBeanFactory
