/**
 *
 * Copyright 2013 Martin Goellnitz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.tangram.components.spring;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;
import org.tangram.spring.StreamingMultipartResolver;
import org.tangram.view.AbstractRequestParameterAccess;


/**
 * Implements a request blob wrapper by means of spring's MultiPartRequest.
 */
public class SpringRequestParameterAccess extends AbstractRequestParameterAccess {

    private static final Log log = LogFactory.getLog(SpringRequestParameterAccess.class);


    /**
     * Weak visibility to avoid direct instanciation.
     */
    @SuppressWarnings("unchecked")
    SpringRequestParameterAccess(HttpServletRequest request) throws Exception {
        if (request instanceof DefaultMultipartHttpServletRequest) {
            DefaultMultipartHttpServletRequest r = (DefaultMultipartHttpServletRequest) request;
            Map<String, MultipartFile> fileMap = r.getFileMap();

            for (Entry<String, MultipartFile> entry : fileMap.entrySet()) {
                if (entry.getValue().getContentType().equals(StreamingMultipartResolver.ERROR)) {
                    throw new Exception(entry.getValue().getOriginalFilename());
                } // if
                String key = entry.getKey();
                String filename = entry.getValue().getName();
                if (log.isInfoEnabled()) {
                    log.info("() size "+entry.getValue().getSize());
                    log.info("() name "+filename);
                } // if
                final String originalFilename = entry.getValue().getOriginalFilename();
                if (log.isDebugEnabled()) {
                    log.debug("() key "+key);
                    log.debug("() original filename "+originalFilename);
                } // if
                if (filename.length()>0) {
                    if (log.isInfoEnabled()) {
                        log.info("multipart file "+key);
                    } // if
                    try {
                        originalNames.put(key, originalFilename);
                        blobs.put(key, entry.getValue().getBytes());
                    } catch (IOException ex) {
                        log.error("()", ex);
                    } // try/catch
                } // if
            } // for
        } // if
        parameterMap = request.getParameterMap();
    } // SpringRequestParameterAccess()

} // SpringRequestParameterAccess
