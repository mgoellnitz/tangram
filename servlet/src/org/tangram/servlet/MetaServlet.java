/**
 *
 * Copyright 2013 Martin Goellnitz
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
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tangram.components.MetaLinkHandler;
import org.tangram.view.ViewContext;
import org.tangram.view.ViewContextFactory;
import org.tangram.view.ViewUtilities;


/**
 * Servlet and component implementation of the "meta controller" front controller.
 */
@Named
public class MetaServlet extends HttpServlet {

    private static final Log log = LogFactory.getLog(MetaServlet.class);

    @Inject
    protected ViewUtilities viewUtilities;

    @Inject
    protected ViewContextFactory viewContextFactory;

    @Inject
    private MetaLinkHandler handler;


    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            ViewContext context = handler.handleRequest(request, response);
            if (context!=null) {
                viewUtilities.render(null, context.getModel(), context.getViewName());
            } // if
        } catch (Throwable ex) {
            ViewContext context = viewContextFactory.createViewContext(ex, request, response);
            if (log.isDebugEnabled()) {
                log.debug("service() caught throwable "+context.getViewName()+"#"+context.getModel().keySet());
            } // if
            response.setContentType("text/html");
            response.setCharacterEncoding("utf-8");
            viewUtilities.render(null, context.getModel(), context.getViewName());
        } // try/catch
    } // service()

} // MetaServlet
