/**
 *
 * Copyright 2011-2014 Martin Goellnitz
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

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Text;
import javax.servlet.ServletRequest;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.content.Content;
import org.tangram.view.AbstractPropertyConverter;


/**
 * Property converter dealing with special blob and text types of the Google App Engine.
 */
public class GaePropertyConverter extends AbstractPropertyConverter {

    private static final Logger LOG = LoggerFactory.getLogger(GaePropertyConverter.class);


    @Override
    public Object createBlob(byte[] octets) {
        return new Blob(octets);
    } // createBlob()


    @Override
    public long getBlobLength(Object o) {
        long result = 0;
        if ((o!=null)&&(isBlobType(o.getClass()))) {
            result = ((Blob) o).getBytes().length;
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
            return StringEscapeUtils.escapeHtml(((Text) o).getValue());
        } else {
            if (o instanceof char[]) {
                return new String((char[]) o);
            } else {
                return super.getEditString(o);
            } // if
        } // if
    } // getEditString()


    /**
     * Only handle special GAE specific cases like Text and Blob.
     *
     * The rest is delegated to the super class implementation.
     */
    @Override
    public Object getStorableObject(Content client, String valueString, Class<? extends Object> cls, ServletRequest request) {
        Object result = super.getStorableObject(client, valueString, cls, request);
        if (result==null) {
            if (cls==Blob.class) {
                LOG.debug("getStorableObject() valueString={}", valueString);
            } else if (cls==Text.class) {
                result = new Text(valueString);
            } else if (cls==char[].class) {
                return valueString.toCharArray();
            } // if
        } // if
        return result;
    } // getStorableObject()

} // GaePropertyConverter
