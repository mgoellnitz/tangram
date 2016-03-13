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
package org.tangram.link;

import javax.servlet.http.HttpServletResponse;

/**
 *
 * Implementing classes extend the link handler by the URL parsing elements.
 *
 * Implementing classes might need the bean factory and the default controller
 * (to register which views are handled by this implementation)
 *
 */
public interface LinkHandler extends LinkFactory {

    /**
     * Return the id of the object to be show, null otherwise.
     *
     * @param url string to parse as a url
     * @param response for error handling
     * @return parsing result as a target descriptor instance
     */
    TargetDescriptor parseLink(String url, HttpServletResponse response);

} // LinkHandler
