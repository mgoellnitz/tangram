package org.tangram.view.velocity;

import java.io.InputStream;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.tangram.content.BeanFactory;
import org.tangram.content.CodeResource;
import org.tangram.content.CodeResourceCache;
import org.tangram.content.Content;

public class VelocityResourceLoader extends ResourceLoader {

    public static CodeResourceCache codeResourceCache;

    public static BeanFactory factory;


    @Override
    public long getLastModified(Resource resource) {
        return System.currentTimeMillis();
    }


    @Override
    public InputStream getResourceStream(String source) throws ResourceNotFoundException {
        // TODO: Streamline this to be sure to use efficient caching
        InputStream result = null; // new ByteArrayInputStream("Oops!".getBytes());
        if ( !"VM_global_library.vm".equals(source)) {
            Content t = factory.getBean(source);
            try {
                if (t!=null) {
                    CodeResource temp = (CodeResource)t;
                    result = temp.getStream();
                } // if
            } catch (Exception e) {
                log.error("getResourceStream()"+e.getMessage());
            } // try/catch
        } // if
        return result;
    } // getResourceStream()


    @Override
    public void init(ExtendedProperties configuration) {
    }


    @Override
    public boolean isSourceModified(Resource resource) {
        return true;
    }

} // VelocityResourceLoader
