/**
 *
 * Copyright 2011-2013 Martin Goellnitz
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
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tangram.Constants;
import org.tangram.annotate.ActionParameter;
import org.tangram.annotate.LinkAction;
import org.tangram.annotate.LinkHandler;
import org.tangram.annotate.LinkPart;
import org.tangram.content.Content;
import org.tangram.link.LinkHandlerRegistry;
import org.tangram.monitor.Statistics;
import org.tangram.mutable.MutableBeanFactory;
import org.tangram.mutable.MutableContent;
import org.tangram.view.TargetDescriptor;


/**
 * Generic tools for repositories with mutable contents.
 *
 * Cache clearing (if necessary), contentExport and import (may need restart of the system afterwards)
 */
@Named
@LinkHandler
public class ToolHandler {

    private static final Log log = LogFactory.getLog(ToolHandler.class);

    @Inject
    private LinkHandlerRegistry registry;

    @Inject
    private Statistics statistics;

    @Inject
    private MutableBeanFactory beanFactory;


    @LinkAction("/clear/caches")
    public TargetDescriptor clearCaches(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (request.getParameter(Constants.ATTRIBUTE_ADMIN_USER)==null) {
            throw new Exception("User may not clear cache");
        } // if

        if (log.isInfoEnabled()) {
            log.info("clearCaches() clearing class specific caches");
        } // if
        for (Class<? extends Content> c : beanFactory.getClasses()) {
            if (c.isInterface()||(c.getModifiers()&Modifier.ABSTRACT)>0) {
                if (log.isInfoEnabled()) {
                    log.info("clearCaches() "+c.getSimpleName()+" may not have instances");
                } // if
            } else {
                beanFactory.clearCacheFor(c);
            } // if
        } // for

        return new TargetDescriptor(statistics, null, null);
    } // clearCaches()


    @LinkAction("/export")
    @SuppressWarnings("rawtypes")
    public TargetDescriptor contentExport(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (request.getAttribute(Constants.ATTRIBUTE_ADMIN_USER)==null) {
            throw new Exception("User may not execute action");
        } // if
        XStream xstream = new XStream(new StaxDriver());
        Collection<Class<? extends MutableContent>> classes = beanFactory.getClasses();

        // Dig out root class of all this evil to find out where id field is defined
        Class<? extends Object> oneClass = classes.iterator().next();
        while (oneClass.getSuperclass()!=Object.class) {
            oneClass = oneClass.getSuperclass();
        } // while
        if (log.isInfoEnabled()) {
            log.info("export() root class to ignore id in: "+oneClass.getName());
        } // if
        xstream.omitField(oneClass, "id");

        for (Class<? extends MutableContent> c : classes) {
            xstream.alias(c.getSimpleName(), c);
            // xstream.omitField(c, "id");
        } // for
        Collection<MutableContent> allContent = new ArrayList<MutableContent>();
        for (Class<? extends MutableContent> c : classes) {
            try {
                allContent.addAll(beanFactory.listBeansOfExactClass(c));
            } catch (Exception e) {
                log.error("export()/list", e);
            } // try/catch
        } // for
        try {
            xstream.toXML(allContent, response.getWriter());
            response.getWriter().flush();
        } catch (IOException e) {
            log.error("export()/toxml", e);
        } // try/catch
        return TargetDescriptor.DONE;
    } // contentExport()


    @SuppressWarnings("unchecked")
    private TargetDescriptor doImport(Reader input, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (request.getAttribute(Constants.ATTRIBUTE_ADMIN_USER)==null) {
            throw new Exception("User may not execute action");
        } // if

        beanFactory.beginTransaction();
        XStream xstream = new XStream(new StaxDriver());
        Collection<Class<? extends MutableContent>> classes = beanFactory.getClasses();
        for (Class<? extends MutableContent> c : classes) {
            xstream.alias(c.getSimpleName(), c);
        } // for

        Object contents = xstream.fromXML(input);
        log.info("read() "+contents);
        if (contents instanceof List) {
            List<? extends MutableContent> list = (List<? extends MutableContent>) contents;
            for (MutableContent o : list) {
                log.info("read() "+o);
                beanFactory.persistUncommitted(o);
            } // for
        } // if
        beanFactory.commitTransaction();

        return TargetDescriptor.DONE;
    } // doImport()


    @LinkAction("/import")
    public TargetDescriptor contentImport(@ActionParameter("xmltext") String xmltext, HttpServletRequest request, HttpServletResponse response) throws Exception {
        return doImport(new StringReader(xmltext), request, response);
    } // contentImport()


    @LinkAction("/import-file/(.*)")
    public TargetDescriptor fileImport(@LinkPart(1) String filename, HttpServletRequest request, HttpServletResponse response) throws Exception {
        return doImport(new FileReader(filename+".xml"), request, response);
    } // fileImport()


    @PostConstruct
    public void afterPropertiesSet() throws Exception {
        registry.registerLinkHandler(this);
    } // afterPropertiesSet()

} // ToolHandler
