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
package org.tangram.ebean;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import org.tangram.content.CodeResource;
import org.tangram.content.Content;
import org.tangram.mutable.MutableCode;


@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@DiscriminatorValue("Code")
public final class Code extends EContent implements MutableCode {

    private String annotation;

    private String mimeType;

    @Column(length = 32000)
    private char[] code;

    private long modificationTime;


    @Override
    public String getAnnotation() {
        return annotation;
    }


    @Override
    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }


    @Override
    public String getMimeType() {
        return mimeType;
    }


    @Override
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }


    public char[] getCode() {
        return code;
    }


    @Override
    public void setCode(char[] code) {
        this.code = code;
    }


    @Override
    public long getModificationTime() {
        return modificationTime;
    }


    @Override
    public void setModificationTime(long modificationTime) {
        this.modificationTime = modificationTime;
    }


    @Override
    public String getCodeText() {
        return code==null ? null : new String(code);
    }


    @Override
    public long getSize() {
        return code.length;
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
