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
package org.tangram.components;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.StringUtils;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.RequiresHttpAction;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.http.client.FormClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.Constants;
import org.tangram.annotate.LinkAction;
import org.tangram.annotate.LinkHandler;
import org.tangram.annotate.LinkPart;
import org.tangram.authentication.AuthenticationService;
import org.tangram.authentication.GenericUser;
import org.tangram.authentication.User;
import org.tangram.link.Link;
import org.tangram.link.LinkFactory;
import org.tangram.link.LinkFactoryAggregator;
import org.tangram.link.LinkHandlerRegistry;
import org.tangram.util.SystemUtils;
import org.tangram.view.TargetDescriptor;


/**
 * Authentication service using pac4j as backend.
 *
 * Supports any client implementation as login provider pac4j supports.
 * Clients are added via dependency injection and are filtered according to the set of login providers
 * according to their respective name - which we tend to set in the DI config files for the clients to
 * be shorter than their default counterparts.
 */
@LinkHandler
@Named
@Singleton
public class PacAuthenticationService implements AuthenticationService, LinkFactory {

    private static final Logger LOG = LoggerFactory.getLogger(PacAuthenticationService.class);

    @Inject
    @Named("loginProviders")
    @Resource(name = "loginProviders")
    private Set<String> loginProviders;

    @Inject
    @Named("userIdAttributes")
    @Resource(name = "userIdAttributes")
    private Map<String, String> userIdAttributes;

    @Inject
    @SuppressWarnings("rawtypes")
    private Set<Client> clientSet;

    @Inject
    private LinkHandlerRegistry registry;

    @Inject
    private LinkFactoryAggregator linkFactoryAggregator;

    private Clients clients = null;


    @Override
    public Set<String> getProviderNames() {
        Set<String> providerNames = new HashSet<>();
        LOG.info("getProviderNames() login provider {}", loginProviders);
        LOG.debug("getProviderNames() client set {}", clientSet);
        for (Client<?, ?> client : clientSet) {
            if (loginProviders.contains(client.getName())) {
                providerNames.add(client.getName());
            } // if
        } // for
        LOG.info("getProviderNames() provider names {}", providerNames);
        return providerNames;
    } // getProviderNames()


    private String getLoginAction(Set<String> providers) {
        LOG.debug("getLoginAction() {}", providers);
        return providers.size()==1 ? providers.iterator().next() : "login";
    }     // getLoginAction


    @Override
    public TargetDescriptor getLoginTarget(Set<String> providers) {
        return new TargetDescriptor(this, null, getLoginAction(providers));
    } // if


    @Override
    public Link getLogoutLink(HttpServletRequest request, HttpServletResponse response) {
        return linkFactoryAggregator.createLink(request, response, this, "logout", null);
    } // getLogoutLink()


    @Override
    public void redirectToLogin(HttpServletRequest request, HttpServletResponse response, Set<String> providers) throws IOException {
        Link link = linkFactoryAggregator.createLink(request, response, this, getLoginAction(providers), null);
        response.sendRedirect(link.getUrl());
    } // redirectToLogin()


    private Clients getClients(HttpServletRequest request, HttpServletResponse response) {
        synchronized (this) {
            if (clients==null) {
                @SuppressWarnings("rawtypes")
                List<Client> clientList = new ArrayList<>(clientSet);
                String callbackUri = linkFactoryAggregator.createLink(request, response, this, "callback", null).getUrl();
                String callbackUrl = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+callbackUri;
                LOG.info("getClients() callback url {}", callbackUrl);
                clients = new Clients(callbackUrl, clientList);
                try {
                    FormClient formClient = clients.findClient(FormClient.class);
                    formClient.setLoginUrl(linkFactoryAggregator.createLink(request, response, this, "login-form", null).getUrl());
                } catch (Exception e) {
                    LOG.warn("getClients() {}", e.getMessage());
                } // try/catch
            }  // if
        } // synchronized
        return clients;
    } // getClients()


    @Override
    public Set<User> getUsers(HttpServletRequest request, HttpServletResponse response) {
        Set<User> result = new HashSet<>();
        HttpSession session = request.getSession(false);
        LOG.debug("getUsers() session: {}", session);
        if (session!=null) {
            LOG.debug("getUsers() session id: {}", session.getId());
            Object users = session.getAttribute(Constants.ATTRIBUTE_USERS);
            LOG.info("getUsers({}) users: {}", session.getId(), users);
            if (users!=null) {
                result = SystemUtils.convert(users);
            } // if
        } // if
        return result;
    } // getUser()


    @LinkAction("/login")
    public TargetDescriptor login(HttpServletRequest request, HttpServletResponse response) throws Exception {
        LOG.info("login()");
        response.setContentType(Constants.MIME_TYPE_HTML_UTF8);
        return new TargetDescriptor(this, null, null);
    } // login()


    @LinkAction("/login-form")
    public TargetDescriptor form(HttpServletRequest request, HttpServletResponse response) throws Exception {
        LOG.info("form()");
        response.setContentType(Constants.MIME_TYPE_HTML_UTF8);
        return new TargetDescriptor(this, "form", null);
    } // form()


    private TargetDescriptor getReturnTarget() {
        return new TargetDescriptor(this, null, "return");
    } // getReturnTarget()


    @LinkAction("/logout")
    public TargetDescriptor logout(HttpServletRequest request, HttpServletResponse response) throws Exception {
        LOG.info("logout()");
        Object returnUrl = request.getSession(true).getAttribute(Constants.ATTRIBUTE_RETURN_URL);
        request.getSession(true).invalidate();
        request.getSession(true).setAttribute(Constants.ATTRIBUTE_RETURN_URL, returnUrl);
        return getReturnTarget();
    } // logout()


    @LinkAction("/redirect/(.*)")
    public TargetDescriptor redirect(@LinkPart(1) String provider, HttpServletRequest request, HttpServletResponse response) throws Exception {
        LOG.info("redirect()");
        WebContext context = new J2EContext(request, response);
        Client<?, ?> client = getClients(request, response).findClient(provider);
        LOG.info("redirect() redirecting");
        client.redirect(context, true, false);
        return TargetDescriptor.DONE;
    } // redirect()


    @LinkAction("/callback")
    public TargetDescriptor callback(HttpServletRequest request, HttpServletResponse response) throws Exception {
        LOG.warn("callback()");
        HttpSession session = request.getSession(true);
        Set<User> users = SystemUtils.convert(session.getAttribute(Constants.ATTRIBUTE_USERS));
        LOG.info("callback() logged in users {}", users);
        if (users==null) {
            users = new HashSet<>();
            session.setAttribute(Constants.ATTRIBUTE_USERS, users);
        } // if
        WebContext context = new J2EContext(request, response);
        final Client<Credentials, UserProfile> client = SystemUtils.convert(getClients(request, response).findClient(context));
        LOG.info("callback() client: {}", client);
        try {
            Credentials credentials = client.getCredentials(context);
            LOG.info("callback() credentials: {}", credentials);
            UserProfile userProfile = client.getUserProfile(credentials, context);
            LOG.debug("callback() userProfile {}: {} ({})", userProfile.getId(), userProfile, request.getSession(false));
            String idAttribute = userIdAttributes.get(client.getName());
            String userId = StringUtils.isEmpty(idAttribute) ? userProfile.getId() : ""+userProfile.getAttribute(idAttribute);
            GenericUser user = new GenericUser(client.getName(), userId, userProfile.getAttributes());
            if (!users.contains(user)) {
                users.add(user);
            } // if
            LOG.info("callback({}) logged in users after callback {}", session.getId(), users);
        } catch (RequiresHttpAction|RuntimeException e) {
            LOG.warn("callback() {}", e.getLocalizedMessage(), e);
            session.setAttribute("tangram.login.error", e.getLocalizedMessage());
            return new TargetDescriptor(this, null, client.getName());
        } // try/catch
        return getReturnTarget();
    } // callback()


    @Override
    public Link createLink(HttpServletRequest request, HttpServletResponse response, Object bean, String action, String view) {
        Link result = null;
        if (bean==this) {
            if ("form".equals(view)) {
                result = new Link("/login-form");
            } else {
                Set<String> providerNames = getProviderNames();
                LOG.info("createLink() creating login handling url for action {} in {}", action, providerNames);
                if ("return".equals(action)) {
                    HttpSession session = request.getSession(true);
                    Object returnUrl = session.getAttribute(Constants.ATTRIBUTE_RETURN_URL);
                    if (returnUrl!=null) {
                        LOG.info("createLink() creating return link to {}", returnUrl);
                        result = new Link(returnUrl.toString().substring(linkFactoryAggregator.getPrefix(request).length()));
                        session.removeAttribute("tangramURL");
                    } else {
                        result = new Link("/");
                    } // if
                } else {
                    result = providerNames.contains(action) ? new Link("/redirect/"+action) : new Link("/"+(action==null ? "login" : action));
                } // if
                LOG.info("createLink() creating login handling url for action {} in {}: {}", action, providerNames, result);
            } // if
        } // if
        return result;
    } // createLink()


    @PostConstruct
    public void afterPropertiesSet() {
        LOG.debug("afterPropertiesSet() registering handler");
        registry.registerLinkHandler(this);
        linkFactoryAggregator.registerFactory(this);
        LOG.debug("afterPropertiesSet() collecting clients");
        for (Client<?, ?> client : clientSet) {
            LOG.info("afterPropertiesSet() client {}", client.getName());
        } // for
    } // afterPropertiesSet()

} // PacAuthenticationService
