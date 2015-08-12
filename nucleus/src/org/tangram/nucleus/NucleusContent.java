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
package org.tangram.nucleus;

import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import org.datanucleus.identity.OID;
import org.datanucleus.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.jdo.JdoContent;


@PersistenceCapable(identityType = IdentityType.DATASTORE)
@DatastoreIdentity(strategy = IdGeneratorStrategy.INCREMENT)
@Inheritance(strategy = InheritanceStrategy.COMPLETE_TABLE)
public abstract class NucleusContent extends JdoContent {

    private static final Logger LOG = LoggerFactory.getLogger(NucleusContent.class);


    @Override
    public String postprocessPlainId(Object id) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("postprocessPlainId() id="+id+" ("+(id==null ? "-" : id.getClass().getName())+")");
        } // if
        if (id instanceof OID) {
            OID oid = (OID) id;
            String pcClass = oid.getPcClass();
            int idx = pcClass.lastIndexOf('.');
            pcClass = pcClass.substring(idx+1);
            return pcClass+":"+oid.getKeyValue();
        } else {
            LOG.warn("postprocessPlainId() returning default '{}'", id);
            return ""+id;
        } // if
    } // postprocessPlainId()


    /* utility helpers until we understand to do this natively in the datanucleus / mongoDB layer (or similar layers) */

    protected byte[] stringToByteArray(String data) {
        return data==null ? null : Base64.decode(data);
    } // stringToByteArray()


    protected String byteArraytoString(byte[] data) {
        return data==null ? null : String.valueOf(Base64.encode(data));
    } // byteArraytoString()


    protected char[] stringToCharArray(String data) {
        return data==null ? null : data.toCharArray();
    } // stringToCharArray()


    protected String charArrayToString(char[] data) {
        return data==null ? null : String.valueOf(data);
    } // charArrayToString()

} // NucleusContent
