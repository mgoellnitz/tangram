/**
 *
 * Copyright 2013-2014 Martin Goellnitz
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
package org.tangram.servlet;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.Constants;
import org.tangram.content.BeanFactory;
import org.tangram.content.Content;
import org.tangram.controller.ControllerHook;
import org.tangram.controller.CustomViewProvider;
import org.tangram.controller.RenderingBase;
import org.tangram.link.Link;
import org.tangram.link.LinkFactory;
import org.tangram.link.LinkFactoryAggregator;
import org.tangram.view.Utils;
import org.tangram.view.ViewContext;
import org.tangram.view.ViewContextFactory;
import org.tangram.view.ViewUtilities;


/**
 * Servlet and component implementation of the same url mapping as the spring default controller.
 */
@Named
public class DefaultServlet extends HttpServlet implements CustomViewProvider, LinkFactory {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultServlet.class);

    @Inject
    protected BeanFactory beanFactory;

    @Inject
    protected ViewContextFactory viewContextFactory;

    @Inject
    protected ViewUtilities viewUtilities;

    @Inject
    private Set<ControllerHook> controllerHooks = new HashSet<ControllerHook>();

    private LinkFactoryAggregator linkFactory;

    private HashSet<String> customLinkViews = new HashSet<String>();

    /**
     * URL part pattern to match ID and VIEW based calls
     */
    private static final Pattern PATTERN_ID_VIEW = Pattern.compile("([A-Z][a-zA-Z]+:[0-9]+).view_(.+)");

    /**
     * URL part pattern to match onyl ID based calls
     */
    private static final Pattern PATTERN_ID = Pattern.compile("([A-Z][a-zA-Z]+:[0-9]+)");


    public BeanFactory getBeanFactory() {
        return beanFactory;
    }


    public LinkFactoryAggregator getLinkFactory() {
        return linkFactory;
    }


    // do autowiring here so the registration can be done automagically
    @Inject
    public void setLinkFactory(LinkFactoryAggregator linkFactory) {
        this.linkFactory = linkFactory;
        this.linkFactory.registerFactory(this);
    }


    public Set<String> getCustomLinkViews() {
        return customLinkViews;
    } // getCustomLinkViews


    @Override
    public Link createLink(HttpServletRequest request, HttpServletResponse r, Object bean, String action, String view) {
        if (bean instanceof Content) {
            if (!customLinkViews.contains(view==null ? Constants.DEFAULT_VIEW : view)) {
                return RenderingBase.createDefaultLink(bean, action, view);
            } // if
        } // if
        return null;
    } // createLink()


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String uri = request.getRequestURI().substring(Utils.getUriPrefix(request).length());
        String view = null;
        String id = null;
        if (LOG.isInfoEnabled()) {
            LOG.info("doGet() uri="+uri);
        } // if
        Utils.setPrimaryBrowserLanguageForJstl(request);
        if (uri.indexOf("view")>0) {
            Pattern p = PATTERN_ID_VIEW;
            Matcher matcher = p.matcher(uri);
            if (matcher.find()) {
                id = matcher.group(1);
                view = matcher.group(2);
            } // if
        } else {
            Pattern p = PATTERN_ID;
            Matcher matcher = p.matcher(uri);
            if (matcher.find()) {
                id = matcher.group();
            } // if
        } // if
        try {
            if (LOG.isInfoEnabled()) {
                LOG.info("doGet() view="+view+" id="+id);
            } // if
            Content content = beanFactory.getBean(id);
            if (LOG.isDebugEnabled()) {
                LOG.debug("doGet() content="+content);
            } // if
            if (content==null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "no content with id "+id+" in repository. (Tangram Default Servlet)");
                return;
            } // if
            if (customLinkViews.contains(view==null ? Constants.DEFAULT_VIEW : view)) {
                try {
                    Link redirectLink = getLinkFactory().createLink(request, response, content, null, view);
                    response.setHeader("Location", redirectLink.getUrl());
                    response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
                } catch (Exception e) {
                    LOG.error("doGet() cannot redirect", e);
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "custom view required.");
                } // try/catch
                return;
            } // if
            Map<String, Object> model = viewContextFactory.createModel(content, request, response);
            if (LOG.isDebugEnabled()) {
                LOG.debug("doGet() model="+model);
            } // if
            viewUtilities.render(null, model, view);
            if (LOG.isDebugEnabled()) {
                LOG.debug("doGet() done "+response.getContentType()+" on "+response.getClass().getName());
            } // if
        } catch (Exception e) {
            ViewContext context = viewContextFactory.createViewContext(e, request, response);
            response.setContentType("text/html");
            response.setCharacterEncoding("utf-8");
            viewUtilities.render(null, context.getModel(), context.getViewName());
        } // try/catch
    } // doGet()

} // DefaultServlet
