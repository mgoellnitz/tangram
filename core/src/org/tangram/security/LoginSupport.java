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
package org.tangram.security;

import javax.servlet.http.HttpServletRequest;


/**
 * Instances support the contractor during login handling.
 */
public interface LoginSupport {

    /**
     * Tells if the current instance is a live (non-editable) instance or not.
     * Most implementations will decide t say "no" everytime. Which does definetly not mean, that they
     * are not customer viewable instances but just that the editing instance is the same as the live
     * one.
     */
    boolean isLiveSystem();


    /**
     * If possible store a logout URL in a request attribute.
     * The name of the request attribute is taken from the tangran constants.
     *
     * @param request request to create the logout URL for
     * @param currentURL URL currently requested to display
     */
    void storeLogoutURL(HttpServletRequest request, String currentURL);


    /**
     * Create a login URL redirecting to the currently requested URL after login.
     *
     * @param currentURL URL currently requested (not yet viewes)
     * @return login URL encoding login and current url
     */
    String createLoginURL(String currentURL);

} // LoginSupport
