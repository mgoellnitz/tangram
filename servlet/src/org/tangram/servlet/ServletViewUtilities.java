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
import java.io.Writer;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.inject.Named;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tangram.Constants;
import org.tangram.components.TangramServices;
import org.tangram.view.BufferResponse;
import org.tangram.view.RequestBlobWrapper;
import org.tangram.view.TemplateResolver;
import org.tangram.view.ViewContext;
import org.tangram.view.ViewContextFactory;
import org.tangram.view.ViewUtilities;


@Named
public class ServletViewUtilities implements ViewUtilities {

    private static final Log log = LogFactory.getLog(ServletViewUtilities.class);


    /**
     * Creates a plain servlet api based request blob wrapper.
     *
     * @param request
     * @return request blob wrapper suitable for the given request
     */
    @Override
    public RequestBlobWrapper createWrapper(HttpServletRequest request) {
        return new ServletRequestBlobWrapper(request);
    } // createWrapper()


    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void render(Writer out, Map<String, Object> model, String view) throws IOException {
        view = (view==null) ? Constants.DEFAULT_VIEW : view;
        HttpServletRequest request = (HttpServletRequest) model.get("request");
        HttpServletResponse response = (HttpServletResponse) model.get("response");
        String template = null;
        final Set<TemplateResolver> resolvers = TangramServices.getResolvers();
        if (log.isDebugEnabled()) {
            log.debug("render() resolvers="+resolvers);
        } // if
        for (TemplateResolver<String> resolver : resolvers) {
            if (log.isDebugEnabled()) {
                log.debug("render() resolver="+resolver);
            } // if
            if (template==null) {
                template = resolver.resolveTemplate(view, model, Locale.getDefault());
            } // if
        } // for

        if (log.isDebugEnabled()) {
            log.debug("render() template="+template);
        } // if
        ViewContextFactory vcf = TangramServices.getViewContextFactory();
        ViewContext vc = vcf.createViewContext(model, view);

        RequestDispatcher requestDispatcher = request.getRequestDispatcher(template);
        if (requestDispatcher!=null) {
            try {
                for (String key : model.keySet()) {
                    request.setAttribute(key, model.get(key));
                } // for
                BufferResponse br = new BufferResponse();
                requestDispatcher.include(request, br);
                out.write(br.getContents());
            } catch (ServletException ex) {
                throw new IOException(ex.getCause());
            } // try/catch
        } // if
    } // render()


    @Override
    public void render(Writer out, Object bean, String view, ServletRequest request, ServletResponse response) throws IOException {
        render(out, TangramServices.getViewContextFactory().createModel(bean, request, response), view);
    } // render()

} // ServletViewUtilities
