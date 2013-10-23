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
package org.tangram.gae;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import org.tangram.content.CodeResource;
import org.tangram.content.Content;

import com.google.appengine.api.datastore.Text;

/*
 * TODO: Move to a more specific package to be able to have fewer packages scanned by Tangrams model autodetection
 */
@PersistenceCapable
@Inheritance(customStrategy = "complete-table")
public class Code extends GaeContent implements CodeResource {

    private String annotation;

    private String mimeType;

    @Persistent
    private Text code;


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


    public Text getCode() {
        return code;
    }


    public void setCode(Text code) {
        this.code = code;
    }


    @Override
    public String getCodeText() {
        return (getCode()==null) ? null : getCode().getValue();
    } // getCodeText()


    @Override
    public InputStream getStream() throws Exception {
        byte[] bytes = getCodeText().getBytes("UTF-8");
        return new ByteArrayInputStream(bytes);
    } // getStream()


    @Override
    public long getSize() {
        return getCodeText().length();
    } // getSize()


    @Override
    public int compareTo(Content o) {
        return (o instanceof Code) ? (getMimeType()+getAnnotation()).compareTo(((Code)o).getMimeType()+((Code)o).getAnnotation())
                : super.compareTo(o);
    } // compareTo()

} // Code
