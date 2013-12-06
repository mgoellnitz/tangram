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

public abstract class AbstractRequestBlobWrapper implements RequestBlobWrapper  {

    protected Map<String, byte[]> blobs = new HashMap<String, byte[]>();

    @Override
    public Collection<String> getNames() {
        return blobs.keySet();
    } // getNames()


    @Override
    public byte[] getData(String name) {
        return blobs.get(name);
    } // getData();

} // AbstractRequestBlobWrapper
