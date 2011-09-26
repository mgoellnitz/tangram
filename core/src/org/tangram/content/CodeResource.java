package org.tangram.content;

import java.io.InputStream;

public interface CodeResource extends Content {
    
    String getAnnotation();
    
    String getMimeType();
    
    InputStream getStream() throws Exception;
    
    long getSize();
    
    String getCodeText();

} // CodeResource
