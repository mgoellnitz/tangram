/**
 *
 * Copyright 2011-2015 Martin Goellnitz
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

    /**
     * Get URI prefix for requests handled by tangram view dispatching.
     * @param request current request context
     * @return URI prefix string
     */
    String getPrefix(HttpServletRequest request);


    /**
     * Register a link handler factory to be used in aggregated link generation process.
     * 
     * @param factory factory instance to be registered
     */
    void registerFactory(LinkFactory factory);


    /**
     * Unregister a previously registered link factory instance to be excluded from future aggregated link generation processes.
     * 
     * @param factory factory instance to be unregistered 
     */
    void unregisterFactory(LinkFactory factory);


    /**
     * Create a link for the given bean, action, and view in the context of a request and response.
     *
     * Create a link instance if this link instance is responsible for the given context or null otherwise.
     * @param request request context this link creation takes place in
     * @param response response currently in creation to answer the request
     * @param bean content instance to create a link for
     * @param action action to describe in the created link - may be null
     * @param view view to show the given content item in - may be null for default view
     * @return link instance for the given parameters
     */
    Link createLink(HttpServletRequest request, HttpServletResponse response, Object bean, String action, String view);


    /**
     * Find the method for the given target object.
     *
     * @param target instance to find method in
     * @param methodName name of the method to find
     * @return method instance or null
     */
    Method findMethod(Object target, String methodName);

} // LinkFactoryAggregator
