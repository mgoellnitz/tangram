package org.tangram.rdbms.solution;

import javax.jdo.annotations.PersistenceCapable;

@PersistenceCapable
public class ImageData extends Linkable {

    private byte[] data;

    private String mimeType;

    private String width;

    private String height;


    public byte[] getData() {
        return data;
    }


    public void setData(byte[] data) {
        this.data = data;
    }


    public String getMimeType() {
        return mimeType;
    }


    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }


    public String getWidth() {
        return width;
    }


    public void setWidth(String width) {
        this.width = width;
    }


    public String getHeight() {
        return height;
    }


    public void setHeight(String height) {
        this.height = height;
    }

    public byte[] getBytes() {
        return getData();
    } // getBytes()

} // Image
