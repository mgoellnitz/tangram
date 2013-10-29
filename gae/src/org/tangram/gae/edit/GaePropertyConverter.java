/**
 *
 * Copyright 2011 Martin Goellnitz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.tangram.gae.edit;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Text;
import javax.servlet.ServletRequest;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tangram.conversion.PropertyConverter;

public class GaePropertyConverter extends PropertyConverter {

    private static final Log log = LogFactory.getLog(PropertyConverter.class);


    @Override
    public Object createBlob(byte[] octets) {
        return new Blob(octets);
    } // createBlob()


    @Override
    public long getBlobLength(Object o) {
        long result = 0;
        if (o!=null) {
            if (isBlobType(o.getClass())) {
                result = ((Blob)o).getBytes().length;
            } // if
        } // if
        return result;
    } // getBlobLength()


    @Override
    public boolean isBlobType(Class<?> cls) {
        return Blob.class.equals(cls);
    } // isBlobType()


    @Override
    public boolean isTextType(Class<?> cls) {
        return (Text.class.equals(cls))||(cls==char[].class);
    } // isBlobType()


    @Override
    public String getEditString(Object o) {
        if (o instanceof Text) {
            return StringEscapeUtils.escapeHtml(((Text)o).getValue());
        } else {
            if (o instanceof char[]) {
                return new String((char[])o);
            } else {
                return super.getEditString(o);
            } // if
        } // if
    } // getEditString()


    /**
     * only handle special GAE specific cases like Text and Blob
     */
    @Override
    public Object getStorableObject(String valueString, Class<? extends Object> cls, ServletRequest request) {
        Object result = super.getStorableObject(valueString, cls, request);
        if (result==null) {
            if (cls==Blob.class) {
                if (log.isDebugEnabled()) {
                    log.debug("getStorableObject() valueString="+valueString);
                } // if
            } else if (cls==Text.class) {
                result = new Text(valueString);
            } else if (cls==char[].class) {
                return valueString.toCharArray();
            } // if
        } // if
        return result;
    } // getStorableObject()

} // GaePropertyConverter
