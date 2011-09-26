package org.tangram.rdbms;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.jdo.annotations.PersistenceCapable;

import org.tangram.content.CodeResource;

@PersistenceCapable
public class Code extends RdbmsContent implements CodeResource {

    private String annotation;

    private String mimeType;

    private char[] code;


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
        return code;
    }


    public void setCode(char[] code) {
        this.code = code;
    }


    @Override
    public String getCodeText() {
        return new String(code);
    }


    @Override
    public long getSize() {
        return code.length;
    }


    @Override
    public InputStream getStream() throws Exception {
        return new ByteArrayInputStream(new String(code).getBytes("UTF-8"));
    }

} // Code
