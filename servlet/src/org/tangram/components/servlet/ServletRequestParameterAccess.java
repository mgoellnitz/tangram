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
import java.io.InputStream;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
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
    ServletRequestParameterAccess(HttpServletRequest request, long uploadFileMaxSize) {
        final String reqContentType = request.getContentType();
        if (log.isDebugEnabled()) {
            log.debug("() uploadFileMaxSize="+uploadFileMaxSize);
            log.debug("() request.contentType="+reqContentType);
        } // if
        if (StringUtils.isNotBlank(reqContentType)&&reqContentType.startsWith("multipart/form-data")) {
            ServletFileUpload upload = new ServletFileUpload();
            upload.setFileSizeMax(uploadFileMaxSize);
            try {
                final FileItemIterator fileItemIterator = upload.getItemIterator(request);
                while (fileItemIterator.hasNext()) {
                    FileItemStream item = fileItemIterator.next();
                    String fieldName = item.getFieldName();
                    InputStream stream = item.openStream();
                    if (item.isFormField()) {
                        // TODO: Just one value per parameter name for now - which is sufficient for tangram itself
                        String[] value = new String[1];
                        value[0] = Streams.asString(stream, "UTF-8");
                        if (log.isDebugEnabled()) {
                            log.debug("() request parameter "+fieldName+"='"+value[0]+"'");
                        } // if
                        parameterMap.put(item.getFieldName(), value);
                    } else {
                        try {
                            if (log.isDebugEnabled()) {
                                log.debug("() item "+item.getName()+" :"+item.getContentType());
                            } // if
                            final byte[] bytes = IOUtils.toByteArray(stream);
                            if (bytes.length>0) {
                                originalNames.put(fieldName, item.getName());
                                blobs.put(fieldName, bytes);
                            } // if
                        } catch (IOException ex) {
                            log.error("()", ex);
                            if (ex.getCause() instanceof FileUploadBase.FileSizeLimitExceededException) {
                                throw new RuntimeException(ex.getCause().getMessage());
                            } // if
                        } // try/catcg
                    } // if
                } // for
            } catch (FileUploadException|IOException ex) {
                log.error("()", ex);
            } // try/catch
        } else {
            parameterMap = request.getParameterMap();
        } // if
    } // ServletRequestParameterAccess()

} // ServletRequestParameterAccess
