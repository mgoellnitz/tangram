/**
 * 
 * Copyright 2011-2013 Martin Goellnitz
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
package org.tangram.view.velocity;

import java.io.InputStream;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.tangram.components.CodeResourceCache;
import org.tangram.content.BeanFactory;
import org.tangram.content.CodeResource;
import org.tangram.content.Content;

public class VelocityResourceLoader extends ResourceLoader {

    public static CodeResourceCache codeResourceCache;

    public static BeanFactory factory;


    @Override
    public long getLastModified(Resource resource) {
        return System.currentTimeMillis();
    }


    @Override
    public InputStream getResourceStream(String source) {
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
