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
package org.tangram.view;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;


/**
 * Instances implement the tangram view include mechanism for object oriented templating of generic models.
 *
 * Instances are required to populate the servlet context with the view settings hash maps containg caching
 * time values for images, css, and java script.
 */
public interface ViewUtilities {


    /**
     * Return the view context factory used by the implementing view utilities instance.
     *
     * @return view context factory instance
     */
    ViewContextFactory getViewContextFactory();


    /**
     * create a new request blob mapper instance for the given request.
     *
     * @param request
     * @throws Exception when trouble with the parameters occured like to large blobs
     * @return request blob wrapper suitable for the request
     */
    RequestParameterAccess createParameterAccess(HttpServletRequest request) throws Exception;



    /**
     * Render the given model with a named view to the given writer instaance.
     * Instances to an object oriented view lookup for the Constants.THIS part of the model map.
     *
     * @param writer (response or nested) writer
     * @param model map of named objects resembling the model
     * @param view view name to be used for rendering the model
     * @throws IOException
     */
    void render(Writer writer, Map<String, Object> model, String view) throws IOException;


    /**
     * Just a short cut for createModel() and a subsequent render()
     *
     * @param writer (response or nested) writer
     * @param bean beans to render
     * @param view view name to use for rendering
     * @param request request to do rendering for
     * @param response response used for rendering
     * @throws IOException
     */
    void render(Writer writer, Object bean, String view, ServletRequest request, ServletResponse response) throws IOException;

} // ViewUtilities
