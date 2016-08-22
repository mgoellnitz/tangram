/*
 *
 * Copyright 2016 Martin Goellnitz
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
package org.tangram.coma;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import org.tangram.content.CodeResource;


/**
 * Code implementation in the Coma context.
 */
public class ComaCode extends ComaContent implements CodeResource {

    public ComaCode(String id, String type, Map<String, Object> properties) {
        super(id, type, properties);
    } // ()


    @Override
    public String getAnnotation() {
        return ""+get("annotation");
    }


    @Override
    public String getMimeType() {
        return ""+get("mimeType");
    }


    @Override
    public String getCodeText() {
        return ""+get("code");
    }


    @Override
    public long getModificationTime() {
        return (Long)get("modificationTime");
    }


    @Override
    public long getSize() {
        return getCodeText().length();
    } // getSize()


    @Override
    public InputStream getStream() throws Exception {
        return new ByteArrayInputStream(getCodeText().getBytes("UTF-8"));
    } // getStream()

} // ComaCode
