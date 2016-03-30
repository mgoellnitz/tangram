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
package org.tangram.mock.content;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.tangram.content.CodeResource;
import org.tangram.content.Content;
import org.tangram.mutable.MutableCode;


/**
 * Mock class to use mutable code for tests.
 */
public class MockMutableCode implements MutableCode {

    private String annotation;

    private String mimeType;

    private String codeText;

    private long modificationTime;

    private String id;


    public String getAnnotation() {
        return annotation;
    }


    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }


    public String getMimeType() {
        return mimeType;
    }


    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }


    public String getCodeText() {
        return codeText;
    }


    public void setCodeText(String codeText) {
        this.codeText = codeText;
    }


    public long getModificationTime() {
        return modificationTime;
    }


    public void setModificationTime(long modificationTime) {
        this.modificationTime = modificationTime;
    }


    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }


    @Override
    public void setCode(char[] code) {
        codeText = String.valueOf(code);
    }


    @Override
    public long getSize() {
        return getCodeText().length();
    } // getSize()


    @Override
    public InputStream getStream() throws Exception {
        return new ByteArrayInputStream(getCodeText().getBytes("UTF-8"));
    } // getStream()


    @Override
    public int compareTo(Content o) {
        return (o instanceof CodeResource) ? (getMimeType()+getAnnotation()).compareTo(((CodeResource) o).getMimeType()
                +((CodeResource) o).getAnnotation()) : -1;
    } // compareTo()

} // MockMutableCode
