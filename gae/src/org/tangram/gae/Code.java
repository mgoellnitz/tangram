package org.tangram.gae;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import org.tangram.content.CodeResource;
import org.tangram.content.Content;

import com.google.appengine.api.datastore.Text;

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
    public InputStream getStream() throws Exception {
        byte[] bytes = getCode().getValue().getBytes("UTF-8");
        return new ByteArrayInputStream(bytes);
    } // getStream()


    @Override
	public long getSize() {
        return getCode().getValue().length();
    } // getSize()
    


    @Override
    public String getCodeText() {
        return getCode().getValue();
    } // getCodeText()


    @Override
    public int compareTo(Content o) {
        return (o instanceof Code) ? (getMimeType()+getAnnotation()).compareTo(((Code)o).getMimeType()
                +((Code)o).getAnnotation()) : super.compareTo(o);
    } // compareTo()

} // Code
