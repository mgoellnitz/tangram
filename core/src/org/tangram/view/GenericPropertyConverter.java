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
package org.tangram.view;

import javax.inject.Singleton;
import javax.servlet.ServletRequest;
import org.tangram.content.Content;
import org.tangram.view.AbstractPropertyConverter;


/**
 * Property converter using byte[] as blob and char[] as text.
 *
 * Generic in the sense that we can use it for JDO, JPA and EBean alike.
 */
@Singleton
public class GenericPropertyConverter extends AbstractPropertyConverter {

    @Override
    public Object createBlob(byte[] octets) {
        return octets;
    } // createBlob()


    @Override
    public long getBlobLength(Object o) {
        return (o instanceof byte[]) ? ((byte[]) o).length : 0;
    } // getBlobLength()


    @Override
    public boolean isBlobType(Class<?> cls) {
        return cls==byte[].class;
    } // isBlobType()


    @Override
    public boolean isTextType(Class<?> cls) {
        return cls==char[].class;
    } // isTextType()


    @Override
    public String getEditString(Object o) {
        return (o instanceof char[]) ? new String((char[]) o) : super.getEditString(o);
    } // getEditString()


    @Override
    public Object getStorableObject(Content client, String valueString, Class<? extends Object> cls, ServletRequest request) {
        return (cls==char[].class) ? valueString.toCharArray() : super.getStorableObject(client, valueString, cls, request);
    } // getStorableObject()

} // GenericPropertyConverter
