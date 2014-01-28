/**
 *
 * Copyright 2013 Martin Goellnitz
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

import java.util.Collection;
import java.util.Map;


/**
 * Wraps the blobs ("files") passed in a multipart request.
 *
 * Instances only present this very lean access to the parameter names and their blob content.
 */
public interface RequestParameterAccess {

    /**
     * Instances return a aollection of blob parameter names
     *
     * @return
     */
    Collection<String> getBlobNames();


    /**
     * Return data as byte[] for a blob (type file) parameter in the underlying request.
     *
     * @param name of the blob parameter
     * @return contents of the transfered file as byte[]
     */
    byte[] getData(String name);


    /**
     * Return original file name for a blob (type file) parameter in the underlying request.
     *
     * @param name of the blob parameter
     * @return original file name for the blob
     */
    String getOriginalName(String name);


    /**
     * Return values for named request parameters.
     *
     * Implementations are aware of possible multipart encodings in post requests.
     *
     * @param name name of the parameter
     * @return return the string value of the parameter from the underlying request
     */
    String getParameter(String name);


    /**
     * Return a map with all named parameters values of the underlying request.
     *
     * Implementations are aware of possible multipart encodings in post requests.
     *
     * @return map mapping parameter names to the respective values
     */
    Map<String, String[]> getParameterMap();

} // RequestParameterAccess
