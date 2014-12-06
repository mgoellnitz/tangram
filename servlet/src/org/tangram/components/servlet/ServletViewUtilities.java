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
package org.tangram.components.servlet;

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.Constants;
import org.tangram.components.CodeResourceCache;
import org.tangram.content.CodeResource;
import org.tangram.servlet.ResponseWrapper;
import org.tangram.view.RequestParameterAccess;
import org.tangram.view.TemplateResolver;
import org.tangram.view.ViewContextFactory;
import org.tangram.view.ViewUtilities;


/**
 * View utility implementation for plain servlet environments.
 * Deals with request parameters and rendering of included views.
 */
@Named("viewUtilities")
@Singleton
public class ServletViewUtilities implements ViewUtilities {

    private static final Logger LOG = LoggerFactory.getLogger(ServletViewUtilities.class);

    private static final Pattern ID_PATTRN = Pattern.compile(Constants.ID_PATTERN);

    @Inject
    private ViewContextFactory viewContextFactory;

    @Inject
    private CodeResourceCache codeResourceCache;

    @SuppressWarnings("rawtypes")
    private List<TemplateResolver> resolvers = new ArrayList<TemplateResolver>();

    private final VelocityEngine velocityEngine;

    private long uploadFileMaxSize = 500000;


    public ServletViewUtilities() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("()");
        } // if
        Properties velocityProperties = new Properties();
        try {
            velocityProperties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("tangram/velocity/velocity.properties"));
        } catch (IOException ex) {
            LOG.error("()", ex);
        } // try/catch
        if (LOG.isDebugEnabled()) {
            LOG.debug("() velocityProperties="+velocityProperties);
        } // if
        velocityEngine = new VelocityEngine(velocityProperties);
    } // ServletViewUtilities()


    @Inject
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void setResolvers(Set<TemplateResolver> resolvers) {
        this.resolvers = new ArrayList<>(resolvers);
        Collections.sort(this.resolvers);
    } // setResolvers()


    @Override
    public ViewContextFactory getViewContextFactory() {
        return viewContextFactory;
    }


    public void setUploadFileMaxSize(long uploadFileMaxSize) {
        this.uploadFileMaxSize = uploadFileMaxSize;
    }


    /**
     * Creates a plain servlet api based request blob wrapper.
     *
     * @param request
     * @return request blob wrapper suitable for the given request
     */
    @Override
    public RequestParameterAccess createParameterAccess(HttpServletRequest request) throws Exception {
        return new ServletRequestParameterAccess(request, uploadFileMaxSize);
    } // createParameterAccess()


    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void render(Writer writer, Map<String, Object> model, String view) throws IOException {
        view = (view==null) ? Constants.DEFAULT_VIEW : view;
        String template = null;
        if (LOG.isDebugEnabled()) {
            LOG.debug("render() resolvers="+resolvers);
        } // if
        for (TemplateResolver<String> resolver : resolvers) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("render() resolver="+resolver);
            } // if
            if (template==null) {
                template = resolver.resolveTemplate(view, model, Locale.getDefault());
            } // if
        } // for

        if (LOG.isDebugEnabled()) {
            LOG.debug("render() template="+template);
        } // if
        if (template==null) {
            throw new IOException("no view "+view+" found for model "+model);
        } // if
        HttpServletRequest request = (HttpServletRequest) model.get(Constants.ATTRIBUTE_REQUEST);
        HttpServletResponse response = (HttpServletResponse) model.get(Constants.ATTRIBUTE_RESPONSE);
        if (ID_PATTRN.matcher(template).matches()) {
            // Velocity:
            if (LOG.isDebugEnabled()) {
                LOG.debug("render() Velocity template="+template);
            } // if
            try {
                CodeResource codeResource = codeResourceCache.get(template);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("render() setting content type from "+response.getContentType()+" to "+codeResource.getMimeType()+" on "+response.getClass().getName());
                } // if
                response.setContentType(codeResource.getMimeType());
                response.setCharacterEncoding("UTF-8");
                if (writer==null) {
                    response.getWriter().flush();
                } else {
                    writer.flush();
                } // if
                VelocityContext context = new VelocityContext(model);
                if (!context.containsKey(Constants.ATTRIBUTE_VIEW_UTILITIES)) {
                    context.put(Constants.ATTRIBUTE_VIEW_UTILITIES, this);
                } // if
                // Doesn't work and fails with wrong encoding - but would have used the caching of velocity
                // velocityEngine.getTemplate(template).merge(context, response.getWriter());
                velocityEngine.evaluate(context, response.getWriter(), "tangram-velocity", new StringReader(codeResource.getCodeText()));
            } catch (Exception ex) {
                throw new IOException(ex.getCause()); // NOPMD we want to lose parts of our stack!
            } // try/catch
        } else {
            // JSP:
            RequestDispatcher requestDispatcher = request.getRequestDispatcher(template);
            if (requestDispatcher!=null) {
                try {
                    for (String key : model.keySet()) {
                        request.setAttribute(key, model.get(key));
                    } // for
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("render("+template+") writer "+writer);
                    } // if
                    ResponseWrapper responseWrapper = null;
                    if (writer==null) {
                        responseWrapper = new ResponseWrapper(response);
                        response = responseWrapper;
                    } else {
                        response.getWriter().flush();
                        writer.flush();
                    } // if
                    requestDispatcher.include(request, response);
                    if (writer==null) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("render("+template+") setting content type for "+responseWrapper.getContentType());
                            LOG.debug("render() setting character encoding for "+responseWrapper.getCharacterEncoding());
                        } // if
                        final String contentType = responseWrapper.getContentType();
                        final String characterEncodingSuffix = "; charset="+responseWrapper.getCharacterEncoding();
                        if (LOG.isWarnEnabled()) {
                            LOG.warn("render() original content type "+contentType);
                        } // if
                        String contentHeader = ((contentType!=null)&&contentType.startsWith("text")&&(contentType.indexOf(';')<0)) ? contentType+characterEncodingSuffix : contentType;
                        response.setHeader("Content-Type", contentHeader);
                        responseWrapper.setHeader("Content-Type", contentHeader);
                        for (Map.Entry<String, String> entry : responseWrapper.getHeaders().entrySet()) {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("render() setting header "+entry.getKey()+": "+entry.getValue());
                            } // if
                            response.setHeader(entry.getKey(), entry.getValue());
                        } // for
                    } // if
                } catch (ServletException ex) {
                    LOG.error("render()", ex);
                    throw new IOException("Problem while including JSP", ex);
                } // try/catch
            } // if
        } // if
    } // render()


    @Override
    public void render(Writer writer, Object bean, String view, ServletRequest request, ServletResponse response) throws IOException {
        render(writer, viewContextFactory.createModel(bean, request, response), view);
    } // render()

} // ServletViewUtilities
