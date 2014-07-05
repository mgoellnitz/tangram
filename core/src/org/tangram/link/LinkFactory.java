/**
 *
 * Copyright 2011 Martin Goellnitz
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.tangram.link;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * Factory to create links in a certain context.
 *
 * Implementing classes do not need to handle all context situation but may decide to return null.
 *
 */
public interface LinkFactory {

    /**
     * Create a link instance if this link instance is responsible for the given context or null otherwise.
     * @param request
     * @param response
     * @param bean
     * @param action
     * @param view
     * @return link for the given set of parameters
     */
    Link createLink(HttpServletRequest request, HttpServletResponse response, Object bean, String action, String view);

} // LinkFactory
