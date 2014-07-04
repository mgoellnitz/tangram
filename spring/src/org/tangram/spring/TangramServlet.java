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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.tangram.spring;

import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.View;
import org.tangram.Constants;
import org.tangram.spring.view.ViewHandler;

public class TangramServlet extends DispatcherServlet {

    private static final long serialVersionUID = -5434847968531348468L;

    private ViewHandler viewHandler;


    /**
     * Obtain view handler and expose application scope to the servlet context.
     *
     * resolves the view handler instance from the application context and exposes any bean on that context
     * to the servleet context scope if the servlet layer.
     *
     * @param context (spring) application context
     */
    @Override
    protected void initStrategies(ApplicationContext context) {
        super.initStrategies(context);
        viewHandler = context.getBeansOfType(ViewHandler.class).values().iterator().next();
        for (String name : context.getBeanNamesForType(Object.class)) {
            getServletContext().setAttribute(name, context.getBean(name));
        } // for
        if (viewHandler==null) {
            throw new RuntimeException("no view handler");
        } // if
    } // initStrategies()


    @Override
    protected View resolveViewName(String viewName, Map<String, Object> model, Locale locale, HttpServletRequest request)
            throws Exception {

        View result = viewHandler.resolveView(viewName, model, locale, request);

        if (result == null) {
            Object self = model.get(Constants.THIS);
            throw new Exception("Cannot find view "+viewName+" for "+(self == null ? model : self));
        } // if

        return result;
    } // resolveViewName()

} // TangramServlet
