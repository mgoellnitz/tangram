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
package org.tangram.spring;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.tangram.security.LoginSupport;


/**
 *
 * Interceptor to check if a user is logged in, if we are a live system, or if we should use generic
 * password protection with users preconfigured in an XML config file.
 *
 * loginSupport helper instance to handle non-generic login stuff
 *
 * freeUrls any URL in this set will not be consired protected
 *
 * allowedUsers if not empty only these users are allowed to log in and view contents
 *
 * adminUsers same as allowedUsers (should be a subset of it if allowed users is not empty) but these users get
 * access to the administrational parts of tangram
 */
public class PasswordInterceptor extends HandlerInterceptorAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(PasswordInterceptor.class);

    @Inject
    private LoginSupport loginSupport;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        loginSupport.handleRequest(request, response);
        return super.preHandle(request, response, handler);
    } // preHandle()

} // PasswordInterceptor
