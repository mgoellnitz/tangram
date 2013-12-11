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
package org.tangram.components.servlet;

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
import org.tangram.view.AbstractRequestParameterAccess;


/**
 * Implements the access to blobs passed over as multi part request parameters by plain servlet means.
 *
 * In case of post request even all parameters have to be separately parsed.
 */
public class ServletRequestParameterAccess extends AbstractRequestParameterAccess {

    private static final Log log = LogFactory.getLog(ServletRequestParameterAccess.class);


    /**
     * Weak visibility to avoid direct instanciation.
     */
    @SuppressWarnings("unchecked")
    ServletRequestParameterAccess(HttpServletRequest request) {
        if (log.isDebugEnabled()) {
            log.debug("()");
        } // if
        if (request.getMethod().equals("GET")) {
            parameterMap = request.getParameterMap();
        } else {
            DiskFileItemFactory factory = new DiskFileItemFactory();
            // TODO: Take from configuration
            factory.setSizeThreshold(500000);
            ServletFileUpload upload = new ServletFileUpload(factory);
            // TODO: Take from configuration
            upload.setFileSizeMax(500000);
            try {
                final List<FileItem> fileItems = upload.parseRequest(request);
                if (log.isDebugEnabled()) {
                    log.debug("() items "+fileItems);
                } // if
                for (FileItem item : fileItems) {
                    if (item.isFormField()) {
                        // TODO: Just one value per parameter name for now - which is sufficient for tangram itself
                        String[] value = new String[1];
                        value[0] = item.getString();
                        if (log.isDebugEnabled()) {
                            log.debug("() request parameter "+item.getFieldName()+"='"+value[0]+"'");
                        } // if
                        parameterMap.put(item.getFieldName(), value);
                    } else {
                        try {
                            if (log.isDebugEnabled()) {
                                log.debug("() item "+item.getName()+" :"+item.getContentType());
                            } // if
                            if (item.getSize()>0) {
                                blobs.put(item.getFieldName(), IOUtils.toByteArray(item.getInputStream()));
                            } // if
                        } catch (IOException ex) {
                            log.error("()", ex);
                        } // try/catcg
                    } // if
                } // for
            } catch (FileUploadException ex) {
                log.error("()", ex);
            } // try/catcg
        } // if
    } // ServletRequestParameterAccess()

} // ServletRequestParameterAccess
