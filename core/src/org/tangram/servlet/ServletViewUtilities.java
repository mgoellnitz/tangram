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
import java.util.Map;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.tangram.view.RequestBlobWrapper;
import org.tangram.view.ViewUtilities;

public class ServletViewUtilities implements ViewUtilities {


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
    public void render(Writer out, Map<String, Object> model, String view) throws IOException {
        throw new UnsupportedOperationException("NYI");
    }


    @Override
    public void render(Writer out, Object bean, String view, ServletRequest request, ServletResponse response) throws IOException {
        throw new UnsupportedOperationException("NYI");
    }

} // ServletViewUtilities
