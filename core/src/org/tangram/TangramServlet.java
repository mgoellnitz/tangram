/**
 * 
 * Copyright 2011 Martin Goellnitz
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package org.tangram;

import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.View;
import org.tangram.view.ViewHandler;

public class TangramServlet extends DispatcherServlet {

    private static final long serialVersionUID = -5434847968531348468L;

    // private static Log log = LogFactory.getLog(TangramServlet.class);

    private ViewHandler viewHandler;


    public TangramServlet() {
        // Just for performance testing
        // this.setContextClass(org.tangram.WebApplicationContext.class);
    } // TangramServlet()


    @Override
    protected void initStrategies(ApplicationContext context) {
        super.initStrategies(context);
        viewHandler = context.getBeansOfType(ViewHandler.class).values().iterator().next();
        if (viewHandler==null) {
            throw new RuntimeException("no view handler");
        } // if
    } // initStrategies()


    @Override
    protected View resolveViewName(String viewName, Map<String, Object> model, Locale locale, HttpServletRequest request)
            throws Exception {

        View result = viewHandler.resolveView(viewName, model, locale, request);

        if (result==null) {
            result = super.resolveViewName(viewName, model, locale, request);
        } // if

        return result;
    } // resolveViewName()

} // TangramServlet
