/**
 *
 * Copyright 2011-2015 Martin Goellnitz
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
package org.tangram.components;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.tangram.Constants;
import org.tangram.annotate.LinkAction;
import org.tangram.annotate.LinkHandler;
import org.tangram.content.CodeHelper;
import org.tangram.content.CodeResource;
import org.tangram.link.LinkHandlerRegistry;
import org.tangram.protection.AuthorizationService;
import org.tangram.view.TargetDescriptor;


/**
 * Export all codes in repository in one structured ZIP file.
 */
@Named
@Singleton
@LinkHandler
public class CodeExporter {

    @Inject
    private LinkHandlerRegistry registry;

    @Inject
    private CodeResourceCache codeResourceCache;

    @Inject
    private AuthorizationService authorizationService;

    private static final Set<String> MIME_TYPES = new HashSet<>();


    static {
        MIME_TYPES.add(Constants.MIME_TYPE_XML);
        MIME_TYPES.add(Constants.MIME_TYPE_HTML);
        MIME_TYPES.add(Constants.MIME_TYPE_CSS);
        MIME_TYPES.add(Constants.MIME_TYPE_JS);
        MIME_TYPES.add(Constants.MIME_TYPE_GROOVY);
    }


    private String getFilename(CodeResource code) {
        return code.getAnnotation().replace(';', '_');
    } // getFilename()


    @LinkAction("/codes.zip")
    public TargetDescriptor codes(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!request.getRequestURI().endsWith(".zip")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        } // if
        if (!authorizationService.isAdminUser(request, response)) {
            return authorizationService.getLoginTarget(request);
        } // if

        long now = System.currentTimeMillis();

        response.setContentType("application/x-zip-compressed");

        CRC32 crc = new CRC32();

        ZipOutputStream zos = new ZipOutputStream(response.getOutputStream());
        zos.setComment("Tangram Repository Codes");
        zos.setLevel(9);
        Collection<CodeResource> codes = codeResourceCache.getCodes();
        for (CodeResource code : codes) {
            if (StringUtils.isNotBlank(code.getAnnotation())) {
                String mimeType = code.getMimeType();
                String folder = CodeHelper.getFolder(mimeType);
                String extension = CodeHelper.getExtension(mimeType);
                if (MIME_TYPES.contains(mimeType)) {
                    byte[] bytes = code.getCodeText().getBytes("UTF-8");
                    ZipEntry ze = new ZipEntry(folder+"/"+getFilename(code)+extension);
                    ze.setTime(now);
                    crc.reset();
                    crc.update(bytes);
                    ze.setCrc(crc.getValue());
                    zos.putNextEntry(ze);
                    zos.write(bytes);
                    zos.closeEntry();
                } // if
            } // if
        } // for
        zos.finish();
        zos.close();

        return TargetDescriptor.DONE;
    } // codes()


    @PostConstruct
    public void afterPropertiesSet() {
        registry.registerLinkHandler(this);
    } // afterPropertiesSet()

} // CodeExporter
