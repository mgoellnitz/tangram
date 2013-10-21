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
package org.tangram.view.link;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The link factory implementation deals with all the link handler available in our implementations.
 * 
 * It should be aware of the fact that there are "web application context names" to be prepended to the
 * URLs returned in the Link instances from the underlying handler implementations.
 *
 */
public interface LinkFactory {

    String getPrefix(HttpServletRequest request);


    void registerHandler(LinkHandler handler);


    void unregisterHandler(LinkHandler handler);


    Link createLink(HttpServletRequest request, HttpServletResponse response, Object bean, String action, String view);

} // LinkBuilder
