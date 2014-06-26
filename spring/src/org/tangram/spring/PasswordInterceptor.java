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

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.tangram.Constants;
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

    private Set<String> freeUrls = new HashSet<String>();

    private Set<String> allowedUsers = new HashSet<String>();

    private Set<String> adminUsers = new HashSet<String>();


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
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String thisURL = request.getRequestURI();
        request.setAttribute("tangramURL", thisURL);
        if (LOG.isDebugEnabled()) {
            LOG.debug("preHandle() detected URI "+thisURL);
        } // if

        if (!getFreeUrls().contains(thisURL)) {
            boolean liveSystem = loginSupport.isLiveSystem();
            if (liveSystem) {
                request.setAttribute(Constants.ATTRIBUTE_LIVE_SYSTEM, Boolean.TRUE);
            } // if

            Principal principal = request.getUserPrincipal();
            if (liveSystem) {
                if (principal!=null) {
                    if (adminUsers.contains(principal.getName())) {
                        request.setAttribute(Constants.ATTRIBUTE_ADMIN_USER, Boolean.TRUE);
                    } // if
                } // if
            } else {
                if (principal!=null) {
                    String userName = principal.getName();
                    if (LOG.isInfoEnabled()) {
                        LOG.info("preHandle() checking for user: "+userName);
                    } // if
                    loginSupport.storeLogoutURL(request, thisURL);
                    if (adminUsers.contains(userName)) {
                        request.setAttribute(Constants.ATTRIBUTE_ADMIN_USER, Boolean.TRUE);
                    } // if
                    if ((allowedUsers.size()>0)&&(!allowedUsers.contains(userName))) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn("preHandle() user not allowed to access page: "+userName);
                        } // if
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, userName+" not allowed to view page");
                    } // if
                } else {
                    String loginURL = loginSupport.createLoginURL(thisURL);
                    if (allowedUsers.size()>0) {
                        if (LOG.isInfoEnabled()) {
                            LOG.info("preHandle() no logged in user found");
                        } // if
                        response.sendRedirect(loginURL);
                    } else {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("preHandle() system doesn't need login but perhaps application");
                        } // if
                        request.setAttribute(Constants.ATTRIBUTE_LOGIN_URL, loginURL);
                    } // if
                } // if
            } // if
        } // if

        return super.preHandle(request, response, handler);
    } // preHandle()

} // PasswordInterceptor
