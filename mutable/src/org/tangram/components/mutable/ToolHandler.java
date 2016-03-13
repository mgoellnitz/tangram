/**
 *
 * Copyright 2011-2016 Martin Goellnitz
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
package org.tangram.components.mutable;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.Constants;
import org.tangram.annotate.ActionParameter;
import org.tangram.annotate.LinkAction;
import org.tangram.annotate.LinkHandler;
import org.tangram.content.CodeResource;
import org.tangram.content.CodeResourceCache;
import org.tangram.content.Content;
import org.tangram.link.LinkHandlerRegistry;
import org.tangram.link.TargetDescriptor;
import org.tangram.monitor.Statistics;
import org.tangram.mutable.AppEngineXStream;
import org.tangram.mutable.CodeHelper;
import org.tangram.mutable.MutableBeanFactory;
import org.tangram.protection.AuthorizationService;
import org.tangram.util.SystemUtils;
import org.tangram.view.Utils;


/**
 * Generic tools for repositories with mutable contents.
 *
 * Cache clearing (if necessary), code export, code import, generic content export, and generic content import
 */
@Named
@Singleton
@LinkHandler
public class ToolHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ToolHandler.class);

    @Inject
    private LinkHandlerRegistry registry;

    @Inject
    private Statistics statistics;

    @Inject
    private MutableBeanFactory beanFactory;

    @Inject
    private CodeResourceCache codeResourceCache;

    @Inject
    private AuthorizationService authorizationService;


    /**
     * Add the most common stuff to the response and request on any view returning method.
     */
    private void prepareView(HttpServletRequest request, HttpServletResponse response) {
        request.setAttribute("handler", this);
        request.setAttribute("prefix", Utils.getUriPrefix(request));
        response.setContentType(Constants.MIME_TYPE_HTML);
        response.setCharacterEncoding("UTF-8");
    } // prepareView()


    @LinkAction("/clear/caches")
    public TargetDescriptor clearCaches(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (!authorizationService.isAdminUser(request, response)) {
            return authorizationService.getLoginTarget(request);
        } // if

        LOG.info("clearCaches() clearing class specific caches");
        for (Class<? extends Content> c : beanFactory.getClasses()) {
            if (c.isInterface()||(c.getModifiers()&Modifier.ABSTRACT)>0) {
                LOG.info("clearCaches() {} may not have instances", c.getSimpleName());
            } else {
                beanFactory.clearCacheFor(c);
            } // if
        } // for

        return new TargetDescriptor(statistics, null, null);
    } // clearCaches()


    private String getFilename(CodeResource code) {
        return code.getAnnotation().replace(';', '_');
    } // getFilename()


    @LinkAction("/codes.zip")
    public TargetDescriptor codeExport(HttpServletRequest request, HttpServletResponse response) throws IOException {
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
                if (CodeHelper.getCodeMimeTypes().contains(mimeType)) {
                    try {
                        byte[] bytes = code.getCodeText().getBytes("UTF-8");
                        ZipEntry ze = new ZipEntry(folder+"/"+getFilename(code)+extension);
                        ze.setComment(mimeType);
                        ze.setSize(bytes.length);
                        ze.setTime(now);
                        crc.reset();
                        crc.update(bytes);
                        ze.setCrc(crc.getValue());
                        zos.putNextEntry(ze);
                        zos.write(bytes);
                        zos.closeEntry();
                    } catch (IOException ioe) {
                        LOG.error("codeExport()", ioe);
                    } // try/catch
                } // if
            } // if
        } // for
        zos.finish();
        zos.close();

        return TargetDescriptor.DONE;
    } // codeExport()


    @LinkAction("/codes")
    public TargetDescriptor codeImport(@ActionParameter("zipfile") byte[] zipfile, HttpServletRequest request, HttpServletResponse response) throws Exception {
        authorizationService.throwIfNotAdmin(request, response, "Import should not be called directly");
        if (zipfile==null) {
            throw new Exception("You missed to select an input file.");
        } // if
        ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(zipfile), Charset.forName("UTF-8"));
        for (ZipEntry entry = zip.getNextEntry(); entry!=null; entry = zip.getNextEntry()) {
            final String name = entry.getName();
            if (!name.endsWith("/")) {
                String[] pathAndName = name.split("/");
                String mimetype = entry.getComment();
                if (StringUtils.isEmpty(mimetype)) {
                    mimetype = CodeHelper.getMimetype(pathAndName[0]);
                } // if
                if (CodeHelper.getCodeMimeTypes().contains(mimetype)) {
                    LOG.info("codeImport() {}: {} / {} ({}) ({}/{})", pathAndName[0], pathAndName.length>1 ? pathAndName[1] : "*", mimetype, entry.getComment(), entry.getCompressedSize(), entry.getSize());
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    for (int len = zip.read(buffer); len>=0; len = zip.read(buffer)) {
                        baos.write(buffer, 0, len);
                    } // for
                    byte[] data = baos.toByteArray();
                    LOG.debug("codeImport() code {}", new String(data, "UTF-8"));
                    CodeHelper.updateCode(beanFactory, codeResourceCache, mimetype, pathAndName[1], data, entry.getTime());
                } else {
                    LOG.info("codeImport() ignoring {} for its mime type {}.", name, mimetype);
                } // if
            } else {
                LOG.info("codeImport() {} is a folder.", name);
            } // if
        } // while
        prepareView(request, response);
        return new TargetDescriptor(this, null, null);
    } // codeImport()


    @LinkAction("/export")
    public TargetDescriptor contentExport(@ActionParameter(value = "classes") String classList, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (!authorizationService.isAdminUser(request, response)) {
            return authorizationService.getLoginTarget(request);
        } // if
        response.setContentType(Constants.MIME_TYPE_XML);
        response.setCharacterEncoding("UTF-8");
        // The pure reflection provider is used because of Google App Engines API limitations
        XStream xstream = new AppEngineXStream(new StaxDriver());
        Collection<Class<? extends Content>> classes = beanFactory.getClasses();

        // Dig out root class of all this evil to find out where the id field is defined
        Class<? extends Object> oneClass = classes.iterator().next();
        while (oneClass.getSuperclass()!=Object.class) {
            oneClass = oneClass.getSuperclass();
        } // while
        LOG.info("contentExport() root class to ignore fields in: {}", oneClass.getName());
        xstream.omitField(oneClass, "id");
        xstream.omitField(oneClass, "ebeanInternalId");
        final Class<? extends Content> baseClass = beanFactory.getBaseClass();
        if (baseClass!=oneClass) {
            LOG.info("contentExport() additional base class to ignore fields in: {}", oneClass.getName());
            xstream.omitField(baseClass, "id");
            xstream.omitField(baseClass, "beanFactory");
            xstream.omitField(baseClass, "gaeBeanFactory");
            xstream.omitField(baseClass, "ebeanInternalId");
        } // if

        for (Class<? extends Content> c : beanFactory.getAllClasses()) {
            LOG.info("contentExport() aliasing and ignoring fields for {}", c.getName());
            xstream.omitField(c, "beanFactory");
            xstream.omitField(c, "gaeBeanFactory");
            xstream.omitField(c, "userServices");
            xstream.alias(c.getSimpleName(), c);
        } // for

        List<Class<? extends Content>> sortedClasses = new ArrayList<>();
        sortedClasses.add(beanFactory.getImplementingClasses(CodeResource.class).get(0));

        for (String className : StringUtils.isNotEmpty(classList) ? classList.split(",") : new String[0]) {
            for (Class<? extends Content> c : classes) {
                if (c.getSimpleName().equals(className.trim())) {
                    if (!sortedClasses.contains(c)) {
                        sortedClasses.add(c);
                    } // if
                } // if
            } // for
        } // for

        for (Class<? extends Content> c : classes) {
            if (!sortedClasses.contains(c)) {
                sortedClasses.add(c);
            } // if
        } // for

        LOG.info("contentExport() sorted classes {}", sortedClasses);

        Collection<Content> allContent = new ArrayList<>();
        for (Class<? extends Content> c : sortedClasses) {
            try {
                allContent.addAll(beanFactory.listBeansOfExactClass(c));
            } catch (Exception e) {
                LOG.error("contentExport()/list", e);
            } // try/catch
        } // for
        try {
            xstream.toXML(allContent, response.getWriter());
            response.getWriter().flush();
        } catch (IOException e) {
            LOG.error("contentExport()/toxml", e);
        } // try/catch
        return TargetDescriptor.DONE;
    } // contentExport()


    @LinkAction("/import")
    public TargetDescriptor contentImport(@ActionParameter("xmlfile") byte[] xmlfile, HttpServletRequest request, HttpServletResponse response) throws Exception {
        authorizationService.throwIfNotAdmin(request, response, "Import should not be called directly");
        if (xmlfile==null) {
            throw new Exception("You missed to select an input file.");
        } // if
        Reader input = new StringReader(new String(xmlfile, "UTF-8"));

        beanFactory.beginTransaction();
        XStream xstream = new AppEngineXStream(new StaxDriver());
        Collection<Class<? extends Content>> classes = beanFactory.getClasses();
        for (Class<? extends Content> c : classes) {
            xstream.alias(c.getSimpleName(), c);
        } // for

        Object contents = xstream.fromXML(input);
        LOG.info("doImport() {}", contents);
        if (contents instanceof List) {
            List<? extends Content> list = SystemUtils.convertList(contents);
            for (Content o : list) {
                LOG.info("doImport() {}", o);
                beanFactory.persistUncommitted(o);
            } // for
        } // if
        beanFactory.commitTransaction();

        return TargetDescriptor.DONE;
    } // contentImport()


    @LinkAction("/tools")
    public TargetDescriptor importer(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (!authorizationService.isAdminUser(request, response)) {
            return authorizationService.getLoginTarget(request);
        } // if
        prepareView(request, response);
        return new TargetDescriptor(this, null, null);
    } // importer()


    @PostConstruct
    public void afterPropertiesSet() {
        LOG.debug("afterPropertiesSet()");
        registry.registerLinkHandler(this);
    } // afterPropertiesSet()

} // ToolHandler
