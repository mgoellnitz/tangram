/**
 *
 * Copyright 2011-2014 Martin Goellnitz
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

import java.util.Map;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.tangram.Constants;
import org.tangram.content.Content;
import org.tangram.controller.RenderingBase;
import org.tangram.link.Link;
import org.tangram.link.LinkFactoryAggregator;
import org.tangram.view.TargetDescriptor;
import org.tangram.view.Utils;
import org.tangram.view.ViewContext;


/**
 * This controller implementation provides a default URL format
 *
 * /&lt;prefix&gt;/id_&lt;id&gt(/view_&lt;view&gt)
 *
 * and a way to handle those urls passing the content id and optionally the view name to the content and
 * view layers of the framework.
 */
@Controller
public class DefaultController extends RenderingBase {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultController.class);

    @Inject
    private LinkFactoryAggregator linkFactoryAggregator;


    @RequestMapping(value = "/id_{id}/view_{view}")
    public ModelAndView render(@PathVariable("id") String id, @PathVariable("view") String view, HttpServletRequest request,
                               HttpServletResponse response) {
        try {
            Utils.setPrimaryBrowserLanguageForJstl(request);
            if (LOG.isDebugEnabled()) {
                LOG.debug("render() id="+id);
                LOG.debug("render() view="+view);
            } // if
            Content content = beanFactory.getBean(id);
            if (LOG.isDebugEnabled()) {
                LOG.debug("render() content="+content);
            } // if
            if (content==null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "no content with id "+id+" in repository.");
                return null;
            } // if
            if (linkFactoryAggregator.getCustomLinkViews().contains(view==null ? Constants.DEFAULT_VIEW : view)) {
                Link redirectLink = null;
                try {
                    redirectLink = getLinkFactory().createLink(request, response, content, null, view);
                    response.setHeader("Location", redirectLink.getUrl());
                    response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
                } catch (Exception e) {
                    LOG.error("render() cannot redirect", e);
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "custom view required.");
                } // try/catch
                return null;
            } // if
            Map<String, Object> model = createModel(new TargetDescriptor(content, view, null), request, response);
            ViewContext viewContext = viewContextFactory.createViewContext(model, view);
            return SpringViewUtilities.createModelAndView(viewContext);
        } catch (Exception e) {
            ViewContext viewContext = viewContextFactory.createViewContext(e, request, response);
            return SpringViewUtilities.createModelAndView(viewContext);
        } // try/catch
    } // render()


    @RequestMapping(value = "/id_{id}")
    public ModelAndView render(@PathVariable("id") String id, HttpServletRequest request, HttpServletResponse response) {
        return render(id, null, request, response);
    } // render()


    @Override
    public Link createLink(HttpServletRequest request, HttpServletResponse r, Object bean, String action, String view) {
        if (bean instanceof Content) {
            if (!linkFactoryAggregator.getCustomLinkViews().contains(view==null ? Constants.DEFAULT_VIEW : view)) {
                return RenderingBase.createDefaultLink(bean, action, view);
            } // if
        } // if
        return null;
    } // createLink()

} // DefaultController
