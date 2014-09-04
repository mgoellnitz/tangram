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
package org.tangram.coma;

import org.tangram.feature.blob.MimedBlob;


public class ComaBlob implements MimedBlob {

    private final String contentId;

    private final String propertyName;

    private final String mimeType;

    private final long len;

    private byte[] bytes;


    public ComaBlob(String contentId, String propertyName, String mimeType, long len, byte[] data) {
        this.contentId = contentId;
        this.propertyName = propertyName;
        this.mimeType = mimeType;
        this.len = len;
        this.bytes = data;
    } // ComaBlob()


    public String getContentId() {
        return contentId;
    }


    public String getPropertyName() {
        return propertyName;
    }


    @Override
    public String getMimeType() {
        return mimeType;
    }


    public long getLen() {
        return len;
    }


    @Override
    public byte[] getBytes() {
        return bytes;
    }

} // ComaBlob
