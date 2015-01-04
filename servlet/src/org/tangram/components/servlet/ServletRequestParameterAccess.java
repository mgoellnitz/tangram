/**
 *
 * Copyright 2013-2015 Martin Goellnitz
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.view.AbstractRequestParameterAccess;


/**
 * Implements the access to blobs passed over as multi part request parameters by plain servlet means.
 *
 * In case of post request even all parameters have to be separately parsed.
 */
public class ServletRequestParameterAccess extends AbstractRequestParameterAccess {

    private static final Logger LOG = LoggerFactory.getLogger(ServletRequestParameterAccess.class);


    /**
     * Weak visibility to avoid direct instanciation.
     */
    @SuppressWarnings("unchecked")
    ServletRequestParameterAccess(HttpServletRequest request, long uploadFileMaxSize) {
        final String reqContentType = request.getContentType();
        LOG.debug("() uploadFileMaxSize={} request.contentType={}", uploadFileMaxSize, reqContentType);
        if (StringUtils.isNotBlank(reqContentType)&&reqContentType.startsWith("multipart/form-data")) {
            ServletFileUpload upload = new ServletFileUpload();
            upload.setFileSizeMax(uploadFileMaxSize);
            try {
                for (FileItemIterator itemIterator = upload.getItemIterator(request); itemIterator.hasNext();) {
                    FileItemStream item = itemIterator.next();
                    String fieldName = item.getFieldName();
                    InputStream stream = item.openStream();
                    if (item.isFormField()) {
                        String[] value = parameterMap.get(item.getFieldName());
                        int i = 0;
                        if (value==null) {
                            value = new String[1];
                        } else {
                            String[] newValue = new String[value.length+1];
                            System.arraycopy(value, 0, newValue, 0, value.length);
                            i = value.length;
                            value = newValue;
                        } // if
                        value[i] = Streams.asString(stream, "UTF-8");
                        LOG.debug("() request parameter {}='{}'", fieldName, value[0]);
                        parameterMap.put(item.getFieldName(), value);
                    } else {
                        try {
                            LOG.debug("() item {} :{}", item.getName(), item.getContentType());
                            final byte[] bytes = IOUtils.toByteArray(stream);
                            if (bytes.length>0) {
                                originalNames.put(fieldName, item.getName());
                                blobs.put(fieldName, bytes);
                            } // if
                        } catch (IOException ex) {
                            LOG.error("()", ex);                             
                            if (ex.getCause() instanceof FileUploadBase.FileSizeLimitExceededException) {
                                throw new RuntimeException(ex.getCause().getMessage()); // NOPMD we want to lose parts of our stack trace!
                            } // if
                        } // try/catch
                    } // if
                } // for
            } catch (FileUploadException|IOException ex) {
                LOG.error("()", ex);
            } // try/catch
        } else {
            parameterMap = request.getParameterMap();
        } // if
    } // ServletRequestParameterAccess()

} // ServletRequestParameterAccess
