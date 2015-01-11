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
import java.security.Principal;
import java.util.HashSet;
import java.util.Set;
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
import org.tangram.Constants;
import org.tangram.security.LoginSupport;
import org.tangram.util.SetupUtils;


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

    protected Set<String> freeUrls = new HashSet<>();

    protected Set<String> allowedUsers = new HashSet<>();

    protected Set<String> adminUsers = new HashSet<>();


    @Inject
    public void setLoginSupport(LoginSupport loginSupport) {
        PasswordFilter.loginSupport = loginSupport;
    }


    public Set<String> getFreeUrls() {
        return freeUrls;
    }


    public void setFreeUrls(Set<String> freeUrls) {
        this.freeUrls = freeUrls;
    }


    public Set<String> getAllowedUsers() {
        return allowedUsers;
    }


    public void setAllowedUsers(Set<String> allowedUsers) {
        this.allowedUsers = allowedUsers;
    }


    public Set<String> getAdminUsers() {
        return adminUsers;
    }


    public void setAdminUsers(Set<String> adminUsers) {
        this.adminUsers = adminUsers;
    }


    @Override
    public void destroy() {
    } // destroy()


    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        String thisURL = request.getRequestURI();
        request.setAttribute("tangramURL", thisURL);
        LOG.debug("doFilter({}) detected URI {}", this, thisURL);
        LOG.debug("doFilter() allowed users {}", allowedUsers);
        LOG.debug("doFilter() admin users {}", adminUsers);

        if (!getFreeUrls().contains(thisURL)) {
            boolean liveSystem = loginSupport.isLiveSystem();
            request.setAttribute(Constants.ATTRIBUTE_LIVE_SYSTEM, liveSystem);

            Principal principal = request.getUserPrincipal();
            if (liveSystem) {
                if ((principal!=null)&&(adminUsers.contains(principal.getName()))) {
                    request.setAttribute(Constants.ATTRIBUTE_ADMIN_USER, Boolean.TRUE);
                } // if
            } else {
                if (principal!=null) {
                    String userName = principal.getName();
                    LOG.info("doFilter() checking for user: {}", userName);
                    loginSupport.storeLogoutURL(request, thisURL);
                    if (adminUsers.contains(userName)) {
                        request.setAttribute(Constants.ATTRIBUTE_ADMIN_USER, Boolean.TRUE);
                    } // if
                    if ((allowedUsers.size()>0)&&(!allowedUsers.contains(userName))) {
                        LOG.warn("preHandle() user not allowed to access page: {}", userName);
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, userName+" not allowed to view page");
                    } // if
                } else {
                    String loginURL = loginSupport.createLoginURL(thisURL);
                    if (allowedUsers.size()>0) {
                        LOG.info("doFilter() no logged in user found");
                        response.sendRedirect(loginURL);
                    } else {
                        LOG.debug("doFilter() system doesn't need login but perhaps application");
                        request.setAttribute(Constants.ATTRIBUTE_LOGIN_URL, loginURL);
                    } // if
                } // if
            } // if
        } // if

        chain.doFilter(request, response);
    } // afterCompletion()


    @Override
    public void init(FilterConfig config) throws ServletException {
        freeUrls.addAll(SetupUtils.stringSetFromParameterString(config.getInitParameter("free.urls")));
        LOG.info("init() free urls {}", freeUrls);
        allowedUsers.addAll(SetupUtils.stringSetFromParameterString(config.getInitParameter("allowed.users")));
        LOG.info("init() allowed users {}", allowedUsers);
        adminUsers.addAll(SetupUtils.stringSetFromParameterString(config.getInitParameter("admin.users")));
        LOG.info("init() admin users {}", adminUsers);
    } // init()

} // PasswordFilter
