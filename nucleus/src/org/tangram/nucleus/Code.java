/**
 *
 * Copyright 2011-2013 Martin Goellnitz
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
package org.tangram.nucleus;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.jdo.annotations.PersistenceCapable;
import org.tangram.content.CodeResource;
import org.tangram.content.Content;
import org.tangram.mutable.MutableCode;

/**
 * Datanucleus generic code implementation.
 *
 * Internally it's very conservative about types and only using strings even for the code, which is
 * externally mappped as a long text (char[]). This might reduce maximum code size using certain store
 * options.
 */
@PersistenceCapable
public class Code extends NucleusContent implements MutableCode {

    private String annotation;

    private String mimeType;

    private String code;


    @Override
    public String getAnnotation() {
        return annotation;
    }


    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }


    @Override
    public String getMimeType() {
        return mimeType;
    }


    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }


    public char[] getCode() {
        return stringToCharArray(code);
    }


    public void setCode(char[] code) {
        this.code = charArraytoString(code);
    }


    @Override
    public String getCodeText() {
        return code;
    }


    @Override
    public long getSize() {
        return code.length();
    }


    @Override
    public InputStream getStream() throws Exception {
        return new ByteArrayInputStream(getCodeText().getBytes("UTF-8"));
    }

    @Override
    public int compareTo(Content o) {
        return (o instanceof Code) ? (getMimeType()+getAnnotation()).compareTo(((CodeResource) o).getMimeType()+((CodeResource) o).getAnnotation())
                : super.compareTo(o);
    } // compareTo()

} // Code
