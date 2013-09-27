/**
 * 
 * Copyright 2011-2013 Martin Goellnitz
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package org.tangram.mongo;

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.tangram.Constants;

/**
 * 
 * Interceptor to check if a user is logged in, or if we should use generic password protection with users preconfigured
 * in an XML config file
 * 
 * allowedUsers if not empty only these users are allowed to log-in.
 * 
 * adminUsers same as allowedUsers (should be a subset of it) but these users get the access to the editing links
 * 
 */
public class PasswordInterceptor extends HandlerInterceptorAdapter {

    private static final Log log = LogFactory.getLog(PasswordInterceptor.class);

    private Set<String> freeUrls;

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
        if (log.isDebugEnabled()) {
            log.debug("preHandle() detected URI "+thisURL);
        } // if

        if ( !getFreeUrls().contains(thisURL)) {
            Principal principal = request.getUserPrincipal();

            if (principal!=null) {
                String userName = principal.getName();
                if (log.isInfoEnabled()) {
                    log.info("preHandle() checking for user: "+userName);
                } // if
                if (adminUsers.contains(userName)) {
                    request.setAttribute(Constants.ATTRIBUTE_ADMIN_USER, Boolean.TRUE);
                } // if
                if ((allowedUsers.size()>0)&&( !allowedUsers.contains(userName))) {
                    if (log.isWarnEnabled()) {
                        log.warn("preHandle() user not allowed to access page: "+userName);
                    } // if
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, userName+" not allowed to view page");
                } // if
            } else {
                String loginURL = "spring_security_login";
                principal = new Principal() {
                    @Override
                    public String getName() {
                        return "admin";
                    } // getName()
                };
                request.setAttribute(Constants.ATTRIBUTE_ADMIN_USER, "true");
                request.setAttribute(Constants.ATTRIBUTE_USER, principal);
                if (allowedUsers.size()>0) {
                    if (log.isInfoEnabled()) {
                        log.info("preHandle() no logged in user found");
                    } // if
                    if (log.isDebugEnabled()) {
                        log.debug("preHandle() redirecting to '"+loginURL+"'");
                    } // if
                    response.sendRedirect(loginURL);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("preHandle() system doesn't need login but perhaps application");
                    } // if
                    request.setAttribute(Constants.ATTRIBUTE_LOGIN_URL, loginURL);
                } // if
            } // if
        } // if

        return super.preHandle(request, response, handler);
    } // preHandle()

} // PasswordInterceptor
