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

import java.io.IOException;
import java.lang.reflect.Modifier;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.tangram.Constants;
import org.tangram.components.CodeResourceCache;
import org.tangram.components.spring.StatisticsController;
import org.tangram.content.Content;
import org.tangram.controller.RenderingController;
import org.tangram.link.Link;
import org.tangram.link.LinkFactory;
import org.tangram.mutable.MutableBeanFactory;


/**
 * Generic tool controller for repositories with mutable contents.
 *
 * Right at the moment it only can clean the content caches and all depending caches.
 */
@Controller
public class ToolController extends RenderingController {

    private static final Log log = LogFactory.getLog(ToolController.class);

    @Inject
    private StatisticsController statisticsController;

    @Inject
    private CodeResourceCache codeResourceCache;


    @Override
    public void setLinkFactory(LinkFactory linkFactory) {
        // Don't register with link generation - this one doesn't generate links
    } // setLinkFactory()


    @RequestMapping(value = "/clear/caches")
    public ModelAndView clearCaches(HttpServletRequest request, HttpServletResponse response) {
        try {
            if (request.getParameter(Constants.ATTRIBUTE_ADMIN_USER)==null) {
                throw new IOException("User may not clear cache");
            } // if

            MutableBeanFactory mutableBeanFactory = (MutableBeanFactory) beanFactory;

            if (log.isInfoEnabled()) {
                log.info("clearCaches() clearing class specific caches");
            } // if
            for (Class<? extends Content> c : mutableBeanFactory.getClasses()) {
                if (c.isInterface()||(c.getModifiers()&Modifier.ABSTRACT)>0) {
                    if (log.isInfoEnabled()) {
                        log.info("clearCaches() "+c.getSimpleName()+" may not have instances");
                    } // if
                } else {
                    mutableBeanFactory.clearCacheFor(c);
                } // if
            } // for

            return modelAndViewFactory.createModelAndView(statisticsController, request, response);
        } catch (Exception e) {
            return modelAndViewFactory.createModelAndView(e, request, response);
        } // try/catch
    } // clearCaches()


    @Override
    public Link createLink(HttpServletRequest request, HttpServletResponse r, Object bean, String action, String view) {
        return null;
    } // createLink()

} // ToolController
