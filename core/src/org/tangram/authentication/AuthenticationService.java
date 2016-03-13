/*
 *
 * Copyright 2015-2016 Martin Goellnitz
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
package org.tangram.authentication;

import java.io.IOException;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.tangram.link.Link;
import org.tangram.link.TargetDescriptor;


/**
 * A provider to obtain the current user from the current request.
 *
 * Instances can be viewed or redirected to with special actions to perform authentication.
 *
 * It is ensured that they can be redirected to with a default view and action and included as a login view
 * with login action.
 */
public interface AuthenticationService {

    /**
     * Obtain the currently logged in users for the given request.
     *
     * @param request current request
     * @param response response instance for the request
     * @return set of user instance - may be empty
     */
    Set<User> getUsers(HttpServletRequest request, HttpServletResponse response);


    /**
     * Authentication services may support more than one underying internal provider to do the actual authentication.
     *
     * @return return a list of symbolic names for providers
     .*/
    Set<String> getProviderNames();


    /**
     * Return the login target for the given set of providers.
     *
     * @param providers set of allowed providers for this login
     * @return target descriptor of the login page
     */
    TargetDescriptor getLoginTarget(Set<String> providers);


    /**
     * Enforce redirect to a login page.
     *
     * @param request current request
     * @param response response instance for the request
     * @param providers set of allowed providers for this login
     * @throws IOException IO related problems may occur on redirection
     */
    void redirectToLogin(HttpServletRequest request, HttpServletResponse response, Set<String> providers) throws IOException;

    /**
     * Provide a link instance for logout.
     *
     * @param request current request
     * @param response response instance for the request
     * @return logout link for the current environment or null of not available
     */
    Link getLogoutLink(HttpServletRequest request, HttpServletResponse response);

} // AuthenticationService
