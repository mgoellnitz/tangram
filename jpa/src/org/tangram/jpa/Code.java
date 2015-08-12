/**
 *
 * Copyright 2013-2014 Martin Goellnitz
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
package org.tangram.jpa;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Lob;
import org.tangram.content.CodeResource;
import org.tangram.content.Content;
import org.tangram.mutable.MutableCode;

/**
 * Implementation of CodeResource interface for use with JPA.
 *
 * This should be final since the system will not be able to deal with classes derived from code,
 * but we want to allow JPA systems to use subclasses to implement persistency ant thus this class
 * can tecnnically speaking not be final.
 */
@Entity
// Annotation needed for OpenJPA - at least
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Code extends JpaContent implements MutableCode {

    private String annotation;

    private String mimeType;

    @Lob
    private String code;


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
        return code==null ? null : code.toCharArray();
    }


    @Override
    public void setCode(char[] code) {
        this.code = code==null ? null : String.valueOf(code);
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
