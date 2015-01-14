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
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.security.LoginSupport;


/**
 * Filter implementation to check if a user is logged, if we are a live system, or if we should use generic
 * password protection with users preconfigured in the application configuration.
 *
 * loginSupport helper instance to handle non-generic login stuff
 *
 * freeUrls any URL in this set will not be consired protected
 *
 * allowedUsers if not empty only these users are allowed to log in and view contents
 *
 * adminUsers same as allowedUsers (should be a subset of it if allowed users is not empty) but these users get
 * access to the administrational parts of tangram
 *
 * This implementation supports configuration via injections, directly via web.xml, or a mix of both.
 */
public class PasswordFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(PasswordFilter.class);

    private static LoginSupport loginSupport;


    @Inject
    public void setLoginSupport(LoginSupport loginSupport) {
        PasswordFilter.loginSupport = loginSupport;
    }


    @Override
    public void destroy() {
    } // destroy()


    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        loginSupport.handleRequest((HttpServletRequest) req, (HttpServletResponse) resp);
        chain.doFilter(req, resp);
    } // afterCompletion()


    @Override
    public void init(FilterConfig config) throws ServletException {
    } // init()

} // PasswordFilter
