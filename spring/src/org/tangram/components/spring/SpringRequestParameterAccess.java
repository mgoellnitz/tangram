/**
 *
 * Copyright 2013-2014 Martin Goellnitz
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;
import org.tangram.spring.StreamingMultipartResolver;
import org.tangram.view.AbstractRequestParameterAccess;


/**
 * Implements a request blob wrapper by means of spring's MultiPartRequest.
 */
public class SpringRequestParameterAccess extends AbstractRequestParameterAccess {

    private static final Logger LOG = LoggerFactory.getLogger(SpringRequestParameterAccess.class);


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
                LOG.info("() name {}", filename);
                LOG.info("() size {}", entry.getValue().getSize());
                final String originalFilename = entry.getValue().getOriginalFilename();
                LOG.debug("() key {} original filename {}", key, originalFilename);
                if (filename.length()>0) {
                    LOG.info("multipart file {}", key);
                    try {
                        originalNames.put(key, originalFilename);
                        blobs.put(key, entry.getValue().getBytes());
                    } catch (IOException ex) {
                        LOG.error("()", ex);
                    } // try/catch
                } // if
            } // for
        } // if
        parameterMap = request.getParameterMap();
    } // SpringRequestParameterAccess()

} // SpringRequestParameterAccess
