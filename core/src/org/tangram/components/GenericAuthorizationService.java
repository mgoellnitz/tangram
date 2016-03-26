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
package org.tangram.components;

import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.Constants;
import org.tangram.authentication.AuthenticationService;
import org.tangram.authentication.User;
import org.tangram.content.BeanListener;
import org.tangram.content.CodeResource;
import org.tangram.content.CodeResourceCache;
import org.tangram.link.Link;
import org.tangram.link.LinkFactoryAggregator;
import org.tangram.link.TargetDescriptor;
import org.tangram.protection.AuthorizationService;
import org.tangram.util.SystemUtils;
import org.tangram.view.Utils;


/**
 * Generic authorization service to encapsule authentication, global application protection, and generic admin role.
 *
 * Meant to check if a the system is globally locked to be only usable by allowed users. Support the maintenance
 * of a generic admin role used by other components and provides some helpers for these components.
 *
 * It is possible to have an additional list of users in admin role by adding an entry "adminUsers" to the code resource
 * item "users.properties" (holding additional user/hashed password mappings for the authentication) having a comma
 * separated list of user's names.
 *
 * freeUrls any URL in this set will not be consired protected
 *
 * allowedUsers if not empty only these users are allowed to log in and view contents for any result of the application
 *
 * adminUsers list of admin users considered valid actors for components using this service
 *
 * loginProviders list of providers the underlying authentication service may take into consideration
 *
 */
@Singleton
@Named("authorizationService")
public class GenericAuthorizationService implements AuthorizationService, BeanListener {

    private static final Logger LOG = LoggerFactory.getLogger(GenericAuthorizationService.class);

    @Inject
    private AuthenticationService authenticationService;

    @Inject
    private LinkFactoryAggregator linkFactoryAggregator;

    @Inject
    private CodeResourceCache codeResourceCache;

    @Inject
    @Named("freeUrls")
    @Resource(name = "freeUrls")
    protected Set<String> freeUrls;

    @Inject
    @Named("allowedUsers")
    @Resource(name = "allowedUsers")
    protected Set<String> allowedUsers;

    @Inject
    @Named("adminUsers")
    @Resource(name = "adminUsers")
    protected Set<String> adminUsers;

    @Inject
    @Named("loginProviders")
    @Resource(name = "loginProviders")
    protected Set<String> loginProviders;

    private Set<String> effectiveAdminUsers;


    @Override
    public boolean isAdminUser(HttpServletRequest request, HttpServletResponse response) {
        Set<User> users = authenticationService.getUsers(request, response);
        boolean result = false;
        for (User user : users) {
            LOG.info("isAdminUser() {} in {}?", user, effectiveAdminUsers);
            result = result||effectiveAdminUsers.contains(user.getId());
        } // for
        return result;
    } // isAdminUser()


    @Override
    public TargetDescriptor getLoginTarget(HttpServletRequest request) {
        String queryString = request.getQueryString();
        String thisURL = request.getRequestURI()+(StringUtils.isEmpty(queryString) ? "" : "?"+queryString);
        LOG.info("getLoginTarget({}) {}", thisURL, loginProviders);
        request.getSession(true).setAttribute(Constants.ATTRIBUTE_RETURN_URL, thisURL);
        return authenticationService.getLoginTarget(loginProviders);
    } // getLoginTarget()


    @Override
    public void throwIfNotAdmin(HttpServletRequest request, HttpServletResponse response, String message) throws Exception {
        if (!isAdminUser(request, response)) {
            throw new Exception(message);
        } // if
    } // throwIfNotAdmin()


    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String thisURL = request.getRequestURI().substring(Utils.getUriPrefix(request).length());
        LOG.debug("handleRequest({}) detected URI {}", this, thisURL);
        LOG.debug("handleRequest() allowed users {} ({})", allowedUsers, allowedUsers.size());
        LOG.debug("handleRequest() free urls {}", freeUrls != null);
        LOG.debug("handleRequest() free urls {} ({})", freeUrls, freeUrls.size());
        if (!freeUrls.contains(thisURL)) {
            Set<User> users = authenticationService.getUsers(request, response);
            boolean closedSystem = !allowedUsers.isEmpty();
            if (isAdminUser(request, response)) {
                request.setAttribute("tangramAdminUser", true);
            } // if
            if (!users.isEmpty()) {
                boolean allowed = false;
                request.setAttribute("tangramLogoutUrl", authenticationService.getLogoutLink(request, response).getUrl());
                for (User user : users) {
                    allowed = allowed||allowedUsers.contains(user.getId());
                } // for
                if ((closedSystem)&&(!allowed)) {
                    LOG.warn("handleRequest() user not allowed to access page: {}", users);
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, users+" not allowed to view page");
                } // if
            } else {
                if (closedSystem) {
                    LOG.info("handleRequest() no logged in user found while application is globally protected");
                    TargetDescriptor target = getLoginTarget(request);
                    Link loginLink = linkFactoryAggregator.createLink(request, response, target.getBean(), target.getAction(), target.getView());
                    response.sendRedirect(loginLink.getUrl());
                } // if
            } // if
        } // if
    } // handleRequest()


    @Override
    public void reset() {
        effectiveAdminUsers = new HashSet<>(adminUsers);
        try {
            LOG.info("reset() reading repository based additional admin users");
            CodeResource code = codeResourceCache.getTypeCache("text/plain").get("users.properties");
            Properties p = new Properties();
            if (code!=null) {
                p.load(code.getStream());
            } // if
            effectiveAdminUsers.addAll(SystemUtils.stringSetFromParameterString(p.getProperty("adminUsers")));
            LOG.info("reset() effective admin user list is {}", effectiveAdminUsers);
        } catch (Exception e) {
            LOG.error("validate() error while reading admin user list", e);
        } // try/catch
    } // reset()


    @PostConstruct
    public void afterPropertiesSet() {
        LOG.debug("afterPropertiesSet()");
        codeResourceCache.addListener(this);
    } // afterPropertiesSet()

} // GenericAuthorizationService
