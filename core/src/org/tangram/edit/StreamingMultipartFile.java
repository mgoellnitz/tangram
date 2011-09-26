package org.tangram.edit;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.io.IOUtils;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;

public class StreamingMultipartFile implements MultipartFile {

    private FileItemStream item;

    private long size = -1;

    byte[] bytes;


    public StreamingMultipartFile(FileItemStream item) throws IOException {
        this.item = item;
        getBytes();
    } // StreamingMultipartFile()


    public String getName() {
        return item.getName();
    }


    public String getOriginalFilename() {
        return item.getFieldName();
    }


    public String getContentType() {
        return item.getContentType();
    }


    public boolean isEmpty() {
        return false;
    }


    public long getSize() {
        if (size>0) {
            try {
                return getBytes().length;
            } catch (IOException e) {
                throw new MultipartException("Something went wrong here");
            } // try/catch
        } // if
        return size;
    } // getSize()


    public byte[] getBytes() throws IOException {
        if (bytes==null) {
            bytes = IOUtils.toByteArray(item.openStream());
        } // if
        return bytes;
    } // getBytes()


    public InputStream getInputStream() throws IOException {
        return item.openStream();
    } // getInputStream()


    public void transferTo(File dest) throws IOException, IllegalStateException {
        throw new UnsupportedOperationException("transfer to file not implemented");
    }
    
} // StreamingMultipartFile