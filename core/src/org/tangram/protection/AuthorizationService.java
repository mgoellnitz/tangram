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
package org.tangram.protection;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.tangram.link.TargetDescriptor;


/**
 * Simple authorization distunguishing only a generic admin role.
 *
 * Instance must handle the passing of all requests depending on configuration.
 */
public interface AuthorizationService {

    /**
     * Tell if the user currently logged in for the given request is allowed to perform sytem operations.
     *
     * @param request current request
     * @param response response instance for the request
     * @return true if the user is on the list of admin users
     */
    boolean isAdminUser(HttpServletRequest request, HttpServletResponse response);


    /**
     * Get login target for system authorization.
     *
     * @param request current request
     * @return target descriptor of login page
     */
    TargetDescriptor getLoginTarget(HttpServletRequest request);


    /**
     * Check if the currently logged in user is in generic admin role - throw exception with given message otherwise.
     *
     * @param request current request
     * @param response response instance for the request
     * @param message exception's error message if actually thrown
     * @throws Exception if currently logged in user is not admin
     */
    void throwIfNotAdmin(HttpServletRequest request, HttpServletResponse response, String message) throws Exception;


    /**
     * Handle given request and check if user may pass or if redirects have to be applied based
     * on some simple application settings.
     *
     * @param request current request
     * @param response response instance for the request
     * @throws IOException exception may occur on writing to the response or on redirecting
     */
    void handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException;

} // AuthorizationService
