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
import java.util.Date;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.tangram.components.CodeResourceCache;
import org.tangram.content.CodeResource;

public class VelocityResourceLoader extends ResourceLoader {

    @SuppressWarnings("hiding")
    private static final Log log = LogFactory.getLog(VelocityResourceLoader.class);

    public static CodeResourceCache codeResourceCache;


    @Override
    public long getLastModified(Resource resource) {
        if (log.isInfoEnabled()) {
            log.info("getLastModified() "+resource.getName()+" "+new Date(codeResourceCache.getLastUpdate()));
        } // if
        return codeResourceCache.getLastUpdate();
    } // getLastModified()


    @Override
    public InputStream getResourceStream(String source) {
        InputStream result = null; // new ByteArrayInputStream("Oops!".getBytes());
        if ( !"VM_global_library.vm".equals(source)) {
            try {
                if (log.isInfoEnabled()) {
                    log.info("getResourceStream() "+source);
                } // if
                CodeResource t = codeResourceCache.get(source);
                if (t!=null) {
                    result = t.getStream();
                } // if
            } catch (Exception e) {
                log.error("getResourceStream() "+e.getMessage());
            } // try/catch
        } // if
        return result;
    } // getResourceStream()


    @Override
    public void init(ExtendedProperties configuration) {
        if (log.isInfoEnabled()) {
            log.info("init() "+configuration);
        } // if
    } // init()


    @Override
    public boolean isSourceModified(Resource resource) {
        if (log.isInfoEnabled()) {
            log.info("isSourceModified() "+resource.getName()+" "+new Date(resource.getLastModified())+" "
                    +new Date(codeResourceCache.getLastUpdate()));
        } // if
        return (codeResourceCache.getLastUpdate() > resource.getLastModified());
    } // isSourceModified()


    @Override
    public boolean resourceExists(String resourceName) {
        if (log.isInfoEnabled()) {
            log.info("resourceExists() "+resourceName);
        } // if
        return (codeResourceCache.get(resourceName)!=null);
    } // resourceExists()

} // VelocityResourceLoader
