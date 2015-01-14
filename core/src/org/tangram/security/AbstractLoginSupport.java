/*
 * 
 * Copyright 2015 Martin Goellnitz
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

import java.io.IOException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.Constants;


/**
 * Base class dealing for login support instances dealing with user lists, urls free to access, redirecting and
 * error issueing.
 * 
 * Meant to check if a user is logged in, if we are a live system, or if we should use generic
 * password protection with users preconfigured in an XML config file.
 *
 * freeUrls any URL in this set will not be consired protected
 *
 * allowedUsers if not empty only these users are allowed to log in and view contents
 *
 * adminUsers same as allowedUsers (should be a subset of it if allowed users is not empty) but these users get
 * access to the administrational parts of tangram
 */
public abstract class AbstractLoginSupport implements LoginSupport {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractLoginSupport.class);

    protected Set<String> freeUrls = new HashSet<>();

    protected Set<String> allowedUsers = new HashSet<>();

    protected Set<String> adminUsers = new HashSet<>();


    public void setFreeUrls(Set<String> freeUrls) {
        this.freeUrls = freeUrls;
    }


    public void setAllowedUsers(Set<String> allowedUsers) {
        this.allowedUsers = allowedUsers;
    }


    public void setAdminUsers(Set<String> adminUsers) {
        this.adminUsers = adminUsers;
    }


    @Override
    public abstract boolean isLiveSystem();


    @Override
    public abstract void storeLogoutURL(HttpServletRequest request, String currentURL);


    @Override
    public abstract String createLoginURL(String currentURL);


    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String thisURL = request.getRequestURI();
        request.setAttribute("tangramURL", thisURL);
        LOG.debug("doFilter({}) detected URI {}", this, thisURL);
        LOG.debug("doFilter() allowed users {}", allowedUsers);
        LOG.debug("doFilter() admin users {}", adminUsers);
        if (!freeUrls.contains(thisURL)) {
            boolean liveSystem = isLiveSystem();
            request.setAttribute(Constants.ATTRIBUTE_LIVE_SYSTEM, liveSystem);

            // TODO: Debug code for CapeDwarf:
            Enumeration<?> attributeNames = request.getAttributeNames();
            while (attributeNames.hasMoreElements()) {
                String attributeName = ""+attributeNames.nextElement();
                LOG.info("preHandle() attribute {}={}", attributeName, request.getAttribute(attributeName));
            } // while
            Map<?, ?> parameterMap = request.getParameterMap();
            for (Object key : parameterMap.keySet()) {
                LOG.info("preHandle() parameter {}={}", key, parameterMap.get(key));
            } // for
            Cookie[] cookies = request.getCookies();
            for (Cookie cookie : cookies) {
                LOG.info("preHandle() cookie {} {} {} {}", cookie.getName(), cookie.getPath(), cookie.getDomain(), cookie.getValue());
            } // for

            Principal principal = request.getUserPrincipal();
            if (liveSystem) {
                if ((principal!=null)&&(adminUsers.contains(principal.getName()))) {
                    request.setAttribute(Constants.ATTRIBUTE_ADMIN_USER, Boolean.TRUE);
                } // if
            } else {
                if (principal!=null) {
                    String userName = principal.getName();
                    LOG.info("preHandle() checking for user: {}", userName);
                    storeLogoutURL(request, thisURL);
                    if (adminUsers.contains(userName)) {
                        request.setAttribute(Constants.ATTRIBUTE_ADMIN_USER, Boolean.TRUE);
                    } // if
                    if ((allowedUsers.size()>0)&&(!allowedUsers.contains(userName))) {
                        LOG.warn("preHandle() user not allowed to access page: {}", userName);
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, userName+" not allowed to view page");
                    } // if
                } else {
                    String loginURL = createLoginURL(thisURL);
                    if (allowedUsers.size()>0) {
                        LOG.info("preHandle() no logged in user found");
                        response.sendRedirect(loginURL);
                    } else {
                        LOG.debug("preHandle() system doesn't need login but perhaps application");
                        request.setAttribute(Constants.ATTRIBUTE_LOGIN_URL, loginURL);
                    } // if
                } // if
            } // if
        } // if
    } // handleRequest()

} // AbstractLoginSupportAbstractLoginSupport
