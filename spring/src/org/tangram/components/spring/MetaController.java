/**
 *
 * Copyright 2011-2016 Martin Goellnitz
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

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.tangram.components.MetaLinkHandler;
import org.tangram.spring.view.SpringViewUtilities;
import org.tangram.view.ViewContext;
import org.tangram.view.ViewContextFactory;


@Controller
public class MetaController extends AbstractController {

    @Inject
    protected ViewContextFactory viewContextFactory;

    @Inject
    private MetaLinkHandler handler;


    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            ViewContext viewContext = handler.handleRequest(request, response);
            return SpringViewUtilities.createModelAndView(viewContext);
        } catch (Throwable ex) {
            ViewContext viewContext = viewContextFactory.createViewContext(ex, request, response);
            return SpringViewUtilities.createModelAndView(viewContext);
        } // try/catch
    } // handleRequestInternal()

} // MetaController
