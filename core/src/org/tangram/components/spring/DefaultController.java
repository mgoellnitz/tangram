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
package org.tangram.components.spring;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.tangram.Constants;
import org.tangram.content.Content;
import org.tangram.controller.CustomViewProvider;
import org.tangram.controller.RenderingBase;
import org.tangram.link.Link;
import org.tangram.view.TargetDescriptor;


/**
 * This controller implementation provides a default URL format
 *
 * /&lt;prefix&gt;/id_&lt;id&gt(/view_&lt;view&gt)
 *
 * and a way to handle those urls passing the content id and optionally the view name to the content and
 * view layers of the framework.
 */
@Controller
public class DefaultController extends RenderingBase implements CustomViewProvider {

    private static final Log log = LogFactory.getLog(DefaultController.class);

    private HashSet<String> customLinkViews = new HashSet<String>();


    public Set<String> getCustomLinkViews() {
        return customLinkViews;
    } // getCustomLinkViews


    @RequestMapping(value = "/id_{id}/view_{view}")
    public ModelAndView render(@PathVariable("id") String id, @PathVariable("view") String view, HttpServletRequest request,
                               HttpServletResponse response) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("render() id="+id);
                log.debug("render() view="+view);
            } // if
            Content content = beanFactory.getBean(id);
            if (log.isDebugEnabled()) {
                log.debug("render() content="+content);
            } // if
            if (content==null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "no content with id "+id+" in repository.");
                return null;
            } // if
            if (customLinkViews.contains(view==null ? Constants.DEFAULT_VIEW : view)) {
                Link redirectLink = null;
                try {
                    redirectLink = getLinkFactory().createLink(request, response, content, null, view);
                    response.setHeader("Location", redirectLink.getUrl());
                    response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
                } catch (Exception e) {
                    log.error("render() cannot redirect", e);
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "custom view required.");
                } // try/catch
                return null;
            } // if
            Map<String, Object> model = createModel(new TargetDescriptor(content, view, null), request, response);
            return modelAndViewFactory.createModelAndView(model, view);
        } catch (Exception e) {
            return modelAndViewFactory.createModelAndView(e, request, response);
        } // try/catch
    } // render()


    @RequestMapping(value = "/id_{id}")
    public ModelAndView render(@PathVariable("id") String id, HttpServletRequest request, HttpServletResponse response) {
        return render(id, null, request, response);
    } // render()


    @Override
    public Link createLink(HttpServletRequest request, HttpServletResponse r, Object bean, String action, String view) {
        if (bean instanceof Content) {
            if (!customLinkViews.contains(view==null ? Constants.DEFAULT_VIEW : view)) {
                return RenderingBase.createDefaultLink(bean, action, view);
            } // if
        } // if
        return null;
    } // createLink()

} // DefaultController
