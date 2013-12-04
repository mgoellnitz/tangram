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
package org.tangram.view;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;


/**
 * Instances implement the tangram view include mechanism for object oriented templating of generic models.
 */
public interface ViewIncluder {

    /**
     * Create a model map containing a bean and request and response objects
     * @param bean
     * @param request
     * @param response
     * @return
     */
    Map<String, Object> createModel(Object bean, ServletRequest request, ServletResponse response);


    void render(Writer out, Map<String, Object> model, String view) throws IOException;


    /**
     * Just a short cut for createModel and a subsequent render()
     *
     * @param out
     * @param bean
     * @param view
     * @param request
     * @param response
     * @throws IOException
     */
    void render(Writer out, Object bean, String view, ServletRequest request, ServletResponse response) throws IOException;

} // ViewIncluder
