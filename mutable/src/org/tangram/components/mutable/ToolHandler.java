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

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tangram.Constants;
import org.tangram.annotate.LinkAction;
import org.tangram.content.Content;
import org.tangram.link.Link;
import org.tangram.link.LinkHandler;
import org.tangram.link.LinkHandlerRegistry;
import org.tangram.monitor.Statistics;
import org.tangram.mutable.MutableBeanFactory;
import org.tangram.view.TargetDescriptor;


/**
 * Generic tools for repositories with mutable contents.
 *
 * Right at the moment it only can clear the content caches and all depending caches.
 *
 * This has been reworked from a spring @controller to a tangram link handler
 */
@Named
public class ToolHandler implements LinkHandler {

    private static final Log log = LogFactory.getLog(ToolHandler.class);

    @Inject
    private LinkHandlerRegistry registry;

    @Inject
    private Statistics statistics;

    @Inject
    private MutableBeanFactory beanFactory;


    @LinkAction
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

        return TargetDescriptor.DONE;
    } // clearCaches()


    @Override
    public Link createLink(HttpServletRequest req, HttpServletResponse resp, Object bean, String view, String action) {
        return null;
    } // createLink()


    @Override
    public Collection<String> getCustomViews() {
        return Collections.emptySet();
    } // getCustomViews()


    @Override
    public TargetDescriptor parseLink(String uri, HttpServletResponse response) {
        TargetDescriptor result = null;
        if ("/clear/caches".equals(uri)) {
            result = new TargetDescriptor(this, null, "clearCaches");
        } // if
        return result;
    } // parseLink()


    @PostConstruct
    public void afterPropertiesSet() throws Exception {
        registry.registerLinkHandler(this);
    } // afterPropertiesSet()

} // ToolHandler
