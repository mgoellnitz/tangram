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
package org.tangram.servlet;

import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.tangram.protection.AuthorizationService;


/**
 * Filter implementation to integrate basic authorization checks from the authorization service.
 */
@Singleton
public class PasswordFilter implements Filter {

    private static AuthorizationService authorizationService;


    @Inject
    public void setAuthorizationService(AuthorizationService authorizationService) {
        PasswordFilter.authorizationService = authorizationService;
    }


    @Override
    public void destroy() {
        // The filter itself doesn't need to do anything on destruction.
    } // destroy()


    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        authorizationService.handleRequest((HttpServletRequest) req, (HttpServletResponse) resp);
        chain.doFilter(req, resp);
    } // afterCompletion()


    @Override
    public void init(FilterConfig config) throws ServletException {
        // The filter itself doesn't need any additional initialization.
    } // init()

} // PasswordFilter
