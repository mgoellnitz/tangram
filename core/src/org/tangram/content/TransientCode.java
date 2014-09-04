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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.tangram.content;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;

/**
 * Not persistable implementation of the code resource interfaces.
 *
 * Instances of this class are used to be serialized to be persistently cached e.g. The code resource cache is
 * therefor filled with transient copies of the code resources found in the repository.
 */
public class TransientCode implements CodeResource, Serializable {

    private static final long serialVersionUID = -4573161886986101943L;

    private final String annotation;

    private final String mimeType;

    private final String id;

    private final String codeText;


    public TransientCode(String annotation, String mimeType, String id, String codeText) {
        this.annotation = annotation;
        this.mimeType = mimeType;
        this.id = id;
        this.codeText = codeText;
    } // TransientCodeResource()


    public TransientCode(CodeResource resource) {
        this.annotation = resource.getAnnotation();
        this.mimeType = resource.getMimeType();
        this.id = resource.getId();
        this.codeText = resource.getCodeText();
    } // TransientCodeResource()


    @Override
    public String getAnnotation() {
        return annotation;
    } // getAnnotation()


    @Override
    public String getMimeType() {
        return mimeType;
    } // getMimeType()


    @Override
    public String getId() {
        return id;
    } // getId()


    @Override
    public String getCodeText() {
        return codeText;
    } // getCodeText()


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
        return (o instanceof TransientCode) ? (getMimeType()+getAnnotation()).compareTo(((CodeResource)o).getMimeType()
                +((CodeResource)o).getAnnotation()) : -1;
    } // compareTo()


    @Override
    public String toString() {
        return getAnnotation()+" ("+getMimeType()+")";
    } // toString()

} // TransientCodeResource
