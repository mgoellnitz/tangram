/**
 * 
 * Copyright 2009
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
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


    @Override
	public String getName() {
        return item.getName();
    }


    @Override
	public String getOriginalFilename() {
        return item.getFieldName();
    }


    @Override
	public String getContentType() {
        return item.getContentType();
    }


    @Override
	public boolean isEmpty() {
        return false;
    }


    @Override
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


    @Override
	public byte[] getBytes() throws IOException {
        if (bytes==null) {
            bytes = IOUtils.toByteArray(item.openStream());
        } // if
        return bytes;
    } // getBytes()


    @Override
	public InputStream getInputStream() throws IOException {
        return item.openStream();
    } // getInputStream()


    @Override
	public void transferTo(File dest) throws IOException, IllegalStateException {
        throw new UnsupportedOperationException("transfer to file not implemented");
    }
    
} // StreamingMultipartFile