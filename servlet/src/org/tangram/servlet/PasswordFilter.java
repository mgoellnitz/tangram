/**
 *
 * Copyright 2011-2013 Martin Goellnitz
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
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tangram.Constants;
import org.tangram.security.LoginSupport;


/**
 * Filter implementation to check if a user is logged in in the google app engine webapp, if we are a live system,
 * or if we should use generic password protection with users preconfigured in an XML config file.
 *
 * liveSuffix should be the name suffix of your live installation as opposed to the development/testing appengine apps
 *
 * statsUrl is a URL which should be available without log-in /s/statsa typically if you use statistics page for keep
 * alive cron job
 *
 * allowedUsers if not empty only these users are allowed to log-in.
 *
 * Can be emails of google accounts or IDs of OpenID accounts
 *
 * adminUsers same as allowedUsers (should be a subset of it) but these users get the access to the editing links
 */
@Named
@Singleton
public class PasswordFilter implements Filter {

    private static final Log log = LogFactory.getLog(PasswordFilter.class);

    private static LoginSupport loginSupport;

    private Set<String> freeUrls;

    private Set<String> allowedUsers = new HashSet<String>();

    private Set<String> adminUsers = new HashSet<String>();


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
        if (log.isDebugEnabled()) {
            log.debug("preHandle() detected URI "+thisURL);
        } // if

        if (!getFreeUrls().contains(thisURL)) {
            boolean liveSystem = loginSupport.isLiveSystem();
            if (liveSystem) {
                request.setAttribute(Constants.ATTRIBUTE_LIVE_SYSTEM, Boolean.TRUE);
            } // if

            Principal principal = request.getUserPrincipal();

            if (liveSystem) {
                if (principal!=null) {
                    String userName = principal.getName();
                    if (adminUsers.contains(userName)) {
                        request.setAttribute(Constants.ATTRIBUTE_ADMIN_USER, Boolean.TRUE);
                    } // if
                } // if
            } else {
                if (principal!=null) {
                    String userName = principal.getName();
                    if (log.isInfoEnabled()) {
                        log.info("preHandle() checking for user: "+userName);
                    } // if
                    loginSupport.storeLogoutURL(request, thisURL);
                    if (adminUsers.contains(userName)) {
                        request.setAttribute(Constants.ATTRIBUTE_ADMIN_USER, Boolean.TRUE);
                    } // if
                    if ((allowedUsers.size()>0)&&(!allowedUsers.contains(userName))) {
                        if (log.isWarnEnabled()) {
                            log.warn("preHandle() user not allowed to access page: "+userName);
                        } // if
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, userName+" not allowed to view page");
                    } // if
                } else {
                    String loginURL = loginSupport.createLoginURL(thisURL);
                    if (allowedUsers.size()>0) {
                        if (log.isInfoEnabled()) {
                            log.info("preHandle() no logged in user found");
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
        } // if

        chain.doFilter(request, response);
    } // afterCompletion()


    @Override
    @SuppressWarnings("rawtypes")
    public void init(FilterConfig config) throws ServletException {
        final Enumeration initParameterNames = config.getInitParameterNames();
        while (initParameterNames.hasMoreElements()) {
            String parameterName = ""+(initParameterNames.nextElement());
            if (parameterName.startsWith("free.url.")) {
                freeUrls.add(config.getInitParameter(parameterName));
            } // if
            if (parameterName.startsWith("allowed.user.")) {
                allowedUsers.add(config.getInitParameter(parameterName));
            } // if
            if (parameterName.startsWith("admin.user.")) {
                adminUsers.add(config.getInitParameter(parameterName));
            } // if
        } // for
    } // init()

} // PasswordFilter
