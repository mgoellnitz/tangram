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
package org.tangram.components.servlet;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import javax.inject.Named;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.tangram.Constants;
import org.tangram.components.CodeResourceCache;
import org.tangram.components.TangramServices;
import org.tangram.view.RequestParameterAccess;
import org.tangram.view.TemplateResolver;
import org.tangram.view.ViewContext;
import org.tangram.view.ViewContextFactory;
import org.tangram.view.ViewUtilities;


@Named
public class ServletViewUtilities implements ViewUtilities {

    private static final Log log = LogFactory.getLog(ServletViewUtilities.class);

    private static final Pattern ID_PATTRN = Pattern.compile(Constants.ID_PATTERN);

    private static VelocityEngine velocityEngine;


    public ServletViewUtilities() {
        if (log.isDebugEnabled()) {
            log.debug("()");
        } // if
        Properties velocityProperties = new Properties();
        try {
            velocityProperties.load(this.getClass().getClassLoader().getResourceAsStream("tangram/velocity/velocity.properties"));
        } catch (IOException ex) {
            log.error("()", ex);
        } // try/catch
        if (log.isDebugEnabled()) {
            log.debug("() velocityProperties="+velocityProperties);
        } // if
        velocityEngine = new VelocityEngine(velocityProperties);
    } // ServletViewUtilities()


    /**
     * Creates a plain servlet api based request blob wrapper.
     *
     * @param request
     * @return request blob wrapper suitable for the given request
     */
    @Override
    public RequestParameterAccess createParameterAccess(HttpServletRequest request) {
        return new ServletRequestParameterAccess(request);
    } // createParameterAccess()


    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void render(Writer out, Map<String, Object> model, String view) throws IOException {
        view = (view==null) ? Constants.DEFAULT_VIEW : view;
        HttpServletRequest request = (HttpServletRequest) model.get("request");
        HttpServletResponse response = (HttpServletResponse) model.get("response");
        String template = null;
        final List<TemplateResolver> resolvers = TangramServices.getResolvers();
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

        if (ID_PATTRN.matcher(template).matches()) {
            // Velocity:
            if (log.isDebugEnabled()) {
                log.debug("render() Velocity template="+template);
            } // if
            VelocityContext context = new VelocityContext(model);
            Writer writer = new StringWriter();
            if (log.isDebugEnabled()) {
                log.debug("render() resource.loader "+velocityEngine.getProperty("resource.loader"));
            } // if
            try {
                CodeResourceCache c = TangramServices.getCodeResourceCache();
                velocityEngine.evaluate(context, writer, "tangram", new InputStreamReader(c.get(template).getStream()));
            } catch (Exception ex) {
                throw new IOException(ex.getCause());
            } // try/catch
            writer.flush();
            final String templateResult = writer.toString();
            if (log.isDebugEnabled()) {
                log.debug("render() result size "+templateResult.length());
            } // if
            if (out!=null) {
                response.getWriter().flush();
                out.flush();
            } // if
            (out==null ? response.getWriter() : out).write(templateResult);
        } else {
            // JSP:
            RequestDispatcher requestDispatcher = request.getRequestDispatcher(template);
            if (requestDispatcher!=null) {
                try {
                    for (String key : model.keySet()) {
                        request.setAttribute(key, model.get(key));
                    } // for
                    if (log.isDebugEnabled()) {
                        // log.debug("render() writer "+out+" "+response.getWriter());
                        log.debug("render() writer "+out);
                    } // if
                    // BufferResponse br = new BufferResponse(response);
                    if (out!=null) {
                        response.getWriter().flush();
                        out.flush();
                    } // if
                    requestDispatcher.include(request, response);
                    // out.write(br.getContents());
                    // response.getOutputStream().write(br.getBytes());
                } catch (ServletException ex) {
                    log.error("render()", ex);
                    throw new IOException("Problem while including JSP", ex.getCause());
                } // try/catch
            } // if
        } // if
    } // render()


    @Override
    public void render(Writer out, Object bean, String view, ServletRequest request, ServletResponse response) throws IOException {
        render(out, TangramServices.getViewContextFactory().createModel(bean, request, response), view);
    } // render()

} // ServletViewUtilities
