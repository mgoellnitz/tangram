/**
 *
 * Copyright 2013-2015 Martin Goellnitz
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

import java.lang.reflect.Type;
import javax.servlet.ServletRequest;
import org.tangram.content.Content;


/**
 * Instances are sed to explicitly convert human readable values from forms to objects and vice versa.
 *
 * Huma readable in this case means mere strings whire are also used on parameter or url passing of values.
 */
public interface PropertyConverter {

    /**
     * get readable / string passable representation of an object.
     *
     * The set of reasonable classes usable with this method depends on the given implementation.
     *
     * @param o object to convert
     * @return string describing o
     */
    String getEditString(Object o);


    /**
     *
     * Convert a string in a request and environment context in to an object.
     *
     * Passed over client bean is never part of the result to avoid circular dependencies
     *
     * @param client content beans this conversion is done for - e.g. wanting to store the result in a property - may be null
     * @param valueString string describing an object
     * @param cls class expected for the result
     * @param request request used when passing the string to convert
     * @return object representation of valueString
     */
    Object getStorableObject(Content client, String valueString, Class<? extends Object> cls, ServletRequest request);


    /**
     *
     * Convert a string in a request and environment context in to an object.
     *
     * Passed over client bean is never part of the result to avoid circular dependencies
     *
     * @param client content beans this conversion is done for - e.g. wanting to store the result in a property - may be null
     * @param valueString string describing an object
     * @param cls class expected for the result
     * @param type optional type descriptor for cls
     * @param request request used when passing the string to convert
     * @return object representation of valueString
     */
    Object getStorableObject(Content client, String valueString, Class<? extends Object> cls, Type type, ServletRequest request);


    /**
     * Does the underlying implementation consider the given type to be a blob.
     *
     * @param cls class to check if its the blob class of this implementation
     * @return true if cls is considered a blob representation
     */
    boolean isBlobType(Class<?> cls);


    /**
     * If o is of class getBlobClass() it returns the blob size.
     *
     * @param o object to obtain blob size for
     * @return blob's size
     */
    long getBlobLength(Object o);


    /**
     * Does the underlying implementation consider the given type to be a (long, structured) text.
     *
     * @param cls class tell if its considered a text representation for this implementation
     * @return true if cls is considered a text representation
     */
    boolean isTextType(Class<?> cls);


    /**
     * Create an instance of the blob type (s.a.) from the given bytes.
     *
     * @param octets bytes for the blob
     * @return object resembling the byte[]
     */
    Object createBlob(byte[] octets);

} // PropertyConverter
