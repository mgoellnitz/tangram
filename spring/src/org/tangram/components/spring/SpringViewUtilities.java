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

import java.io.IOException;
import java.io.Writer;
import java.util.Locale;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.tangram.Constants;
import org.tangram.spring.view.ViewHandler;
import org.tangram.view.RequestParameterAccess;
import org.tangram.view.ViewContext;
import org.tangram.view.ViewContextFactory;
import org.tangram.view.ViewUtilities;


/**
 * Implements the inclusion mechanism of tangram by means of spring views.
 *
 * It might be a good idea to plce this in the view package hierarchy
 */
@Named("viewUtilities")
@Singleton
public class SpringViewUtilities implements ViewUtilities, ServletContextAware {

    private static final Logger LOG = LoggerFactory.getLogger(SpringViewUtilities.class);

    @Inject
    private ViewContextFactory viewContextFactory;

    @Inject
    private ViewHandler viewHandler;

    private Map<String, Object> viewSettings = null;

    private ServletContext servletContext;

    /**
     * Value to be used if there is not view in hash tables and the like where the use of null would not indicate
     * if there is no view or if we didn't look it up up to now.
     */
    public final static View NOT_FOUND_DUMMY = new View() {

        @Override
        public String getContentType() {
            return null;
        }


        @Override
        public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        }

    };


    @Inject
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }


    /**
     * This is "rawtypes" because of google guice's weak injection mechanism.
     */
    public Map<String, Object> getViewSettings() {
        return viewSettings;
    }


    @Inject
    @SuppressWarnings("unchecked")
    public void setViewSettings(@Named("viewSettings") Map<String, Object> viewSettings) {
        if (viewSettings.containsKey("viewSettings")) {
            viewSettings = (Map<String, Object>) (viewSettings.get("viewSettings"));
        } // if
        this.viewSettings = viewSettings;
    } // setViewSettings()


    @Override
    public ViewContextFactory getViewContextFactory() {
        return viewContextFactory;
    }


    /**
     * Creates a spring request blob wrapper.
     *
     * @param request
     * @return request blob wrapper for the given request
     */
    @Override
    public RequestParameterAccess createParameterAccess(HttpServletRequest request) throws Exception {
        return new SpringRequestParameterAccess(request);
    } // createParameterAccess()


    /**
     * This is a direct converter of tangram view context to spring model and view.
     * We consider it a bug that spring's model and view is not an interfaces which would
     * make things a little bit easier and leaner to implement.
     *
     * @param viewContext
     * @return spring model and view describing exactly the same
     */
    public static ModelAndView createModelAndView(ViewContext viewContext) {
        return viewContext==null ? null : new ModelAndView(viewContext.getViewName(), viewContext.getModel());
    } // createModelAndView()


    public void render(Writer out, Map<String, Object> model, String view) throws IOException {
        ServletRequest request = (ServletRequest) model.get("request");
        ServletResponse response = (ServletResponse) model.get("response");

        ViewContext vc = viewContextFactory.createViewContext(model, view);
        ModelAndView mav = SpringViewUtilities.createModelAndView(vc);
        View effectiveView = mav.getView();
        if (LOG.isDebugEnabled()) {
            LOG.debug("render() effectiveView="+effectiveView);
        } // if
        try {
            if (effectiveView==null) {
                String viewName = mav.getViewName();
                if (viewName==null) {
                    viewName = Constants.DEFAULT_VIEW;
                } // if

                effectiveView = viewHandler.resolveView(viewName, mav.getModel(), Locale.getDefault(), request);
            } // if

            if (out!=null) {
                out.flush();
            } // if
            if (LOG.isDebugEnabled()) {
                LOG.debug("render() model="+mav.getModel());
                LOG.debug("render("+mav.getViewName()+") effectiveView="+effectiveView);
            } // if
            effectiveView.render(mav.getModel(), (HttpServletRequest) request, (HttpServletResponse) response);
        } catch (Exception e) {
            LOG.error("render() #"+view, e);
            if (out!=null) {
                out.write(e.getLocalizedMessage());
            } // if
        } // try/catch
    } // render()


    @Override
    public void render(Writer out, Object bean, String view, ServletRequest request, ServletResponse response) throws IOException {
        render(out, viewContextFactory.createModel(bean, request, response), view);
    } // render()


    @PostConstruct
    public void afterPropertiesSet() throws Exception {
        servletContext.setAttribute("viewSettings", viewSettings);
    } // afterPropertiesSet()

} // SpringViewUtilities
