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

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tangram.jdo.JdoContent;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.repackaged.com.google.common.util.Base64;

@PersistenceCapable
@Inheritance(strategy = InheritanceStrategy.NEW_TABLE, customStrategy = "complete-table")
public abstract class GaeContent extends JdoContent {

    private static final Log log = LogFactory.getLog(GaeContent.class);

    @NotPersistent
    private static final boolean encodeIds = false;

    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    @PrimaryKey
    @Extension(vendorName = "datanucleus", key = "gae.encoded-pk", value = "true")
    // This is not really unused - but the compiler thinks so.
    // We might want to redesign this to application IDs anyway
    private String id;


    @Override
    public String postprocessPlainId(Object oid) {
        String result = (oid==null) ? "" : ""+oid;
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

} // GaeContent
