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
package org.tangram.components.mutable;

import java.lang.reflect.Modifier;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.annotate.LinkAction;
import org.tangram.annotate.LinkHandler;
import org.tangram.content.Content;
import org.tangram.link.LinkHandlerRegistry;
import org.tangram.monitor.Statistics;
import org.tangram.mutable.MutableBeanFactory;
import org.tangram.protection.AuthorizationService;
import org.tangram.view.TargetDescriptor;


/**
 * Generic tools for repositories with mutable contents.
 *
 * Cache clearing (if necessary), contentExport and import (may need restart of the system afterwards)
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
    private AuthorizationService authorizationService;


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


    @PostConstruct
    public void afterPropertiesSet() {
        registry.registerLinkHandler(this);
    } // afterPropertiesSet()

} // ToolHandler
