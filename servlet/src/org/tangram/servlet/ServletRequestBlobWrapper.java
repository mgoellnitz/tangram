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
package org.tangram.servlet;

import org.tangram.view.AbstractRequestBlobWrapper;
import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Implements the access to blobs passed over as multi part request parameters by plain servlet means.
 */
public class ServletRequestBlobWrapper extends AbstractRequestBlobWrapper {

    private static final Log log = LogFactory.getLog(ServletRequestBlobWrapper.class);


    public ServletRequestBlobWrapper(HttpServletRequest request) {
        if (log.isDebugEnabled()) {
            log.debug("()");
        } // if
        DiskFileItemFactory factory = new DiskFileItemFactory();
        // TODO: Take from configuration
        factory.setSizeThreshold(500000);
        if (log.isDebugEnabled()) {
            log.debug("() request "+request.getClass().getName()+" "+request);
            log.debug("() request parameters "+request.getParameterMap());
        } // if
        ServletFileUpload upload = new ServletFileUpload(factory);
        // TODO: Take from configuration
        upload.setFileSizeMax(500000);
        try {
            final List<FileItem> fileItems = upload.parseRequest(request);
            if (log.isDebugEnabled()) {
                log.debug("() items "+fileItems);
            } // if
            for (FileItem item : fileItems) {
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("() iterator "+item.getName()+"/"+item.getContentType());
                    } // if
                    blobs.put(item.getFieldName(), IOUtils.toByteArray(item.getInputStream()));
                } catch (IOException ex) {
                    log.error("()", ex);
                } // try/catcg
            } // for
        } catch (FileUploadException ex) {
            log.error("()", ex);
        } // try/catcg
    } // ServletRequestBlobWrapper()

} // ServletRequestBlobWrapper
