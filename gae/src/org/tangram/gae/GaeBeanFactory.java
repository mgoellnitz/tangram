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
     * Return Google App Engine specific factory overrides.
     *
     * This version is as opposed to other bean factory implementations only configurable by one
     * parameter - useHdrDatastore - which is most likely set to true for applications using JDO 3 and up.
     * In this case cross group transactions are enabled.
     *
     * @return map used as a parameter when creating the PersisteneManager instance
     */
    @Override
    protected Map<? extends Object, ? extends Object> getFactoryConfigOverrides() {
        Map<Object, Object> result = new HashMap<>();
        if (isUseHdrDatastore()) {
            result.put("datanucleus.appengine.datastoreEnableXGTransactions", Boolean.TRUE);
        } // if
        return result;
    } // getFactoryConfigOverrides()


    /**
     * Use Google App Engine specific key factory generate the internal object id for us.
     *
     * @param id numeric id part of the object to create the object id for
     * @param kindClass extected JdoContent subtype for the object with the given id
     * @param kind kind part of the id of the object to create and object id for
     * @return implementation specific id object resembling the type and numeric id part of the given object's id
     */
    @Override
    protected Object getObjectId(String id, Class<? extends Content> kindClass, String kind) {
        long numericId = Long.parseLong(id);
        LOG.debug("getObjectId() kind={} numericId=", kind, numericId);
        return KeyFactory.createKey(kind, numericId);
    } // getObjectId()


    /**
     * Overridden version dealing with and old optional Google App Engine Specific ID format.
     *
     * This whole method is only needed for very old repositories with verbatim ID-Strings instead of
     * "Type:Number" references.
     *
     * @param id id in type:number format or long GAE specific version
     * @return bean referred to by id or null if id was null
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
