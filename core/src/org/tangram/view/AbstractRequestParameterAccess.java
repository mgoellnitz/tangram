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
import java.util.HashMap;
import java.util.Map;


public abstract class AbstractRequestParameterAccess implements RequestParameterAccess {

    protected Map<String, byte[]> blobs = new HashMap<String, byte[]>();

    protected Map<String, String> originalNames = new HashMap<String, String>();

    protected Map<String, String[]> parameterMap = new HashMap<>();


    @Override
    public Collection<String> getBlobNames() {
        return blobs.keySet();
    } // getNames()


    @Override
    public byte[] getData(String name) {
        return blobs.get(name);
    } // getData();


    @Override
    public String getOriginalName(String name) {
        return originalNames.get(name);
    } // getOriginalName();


    @Override
    public Map<String, String[]> getParameterMap() {
        return parameterMap;
    } // getParameterMap()


    @Override
    public String getParameter(String name) {
        String[] values = getParameterMap().get(name);
        return (values.length > 0) ? values[0] : null;
    } // getParameter()

} // AbstractRequestParameterAccess
