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
package org.tangram.link;

import java.lang.reflect.Method;
import java.util.Collection;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * A link factory aggregator deals link with generation by deletating to a set of registered different link factories.
 *
 * It should be aware of the fact that there are "web application context names" to be prepended to the
 * URLs returned in the Link instances from the underlying handler implementations.
 *
 */
public interface LinkFactoryAggregator {

    String getPrefix(HttpServletRequest request);


    void registerFactory(LinkFactory handler);


    void unregisterFactory(LinkFactory handler);


    Link createLink(HttpServletRequest request, HttpServletResponse response, Object bean, String action, String view);


    /**
     * Creates a link for the given paraeters using the given set of link handler instances.
     *
     * Ignores the internally registered set of implementations.
     *
     * @param linkkHandlers
     * @param request
     * @param response
     * @param bean
     * @param action
     * @param view
     * @return
     */
    Link createLink(Collection<? extends LinkFactory> handlers, HttpServletRequest request, HttpServletResponse response, Object bean, String action, String view);


    /**
     * Find the method for the given target object.
     *
     * @param target
     * @param methodName
     * @return
     */
    Method findMethod(Object target, String methodName);

} // LinkBuilder
