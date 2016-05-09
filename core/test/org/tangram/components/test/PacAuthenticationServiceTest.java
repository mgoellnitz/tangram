/*
 *
 * Copyright 2016 Martin Goellnitz
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
package org.tangram.components.test;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.pac4j.core.client.Client;
import org.pac4j.http.client.indirect.FormClient;
import org.pac4j.http.profile.UsernameProfileCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.tangram.Constants;
import org.tangram.components.PacAuthenticationService;
import org.tangram.link.Link;
import org.tangram.link.LinkFactoryAggregator;
import org.tangram.link.LinkHandlerRegistry;
import org.tangram.link.TargetDescriptor;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test some aspects of the authentication service.
 */
public class PacAuthenticationServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(PacAuthenticationServiceTest.class);

    @Mock
    private LinkHandlerRegistry linkHandlerRegistry; // NOPMD - this field is not really unused

    @Mock
    private LinkFactoryAggregator aggregator; // NOPMD - this field is not really unused

    @Spy
    @SuppressWarnings("rawtypes")
    private final Set<Client> clientSet = new HashSet<>();

    @Spy
    private final Set<String> loginProviders = new HashSet<>();

    @Spy
    private final Map<String, String> userIdAttributes = new HashMap<>();

    @InjectMocks
    private final PacAuthenticationService pacAuthenticationService = new PacAuthenticationService();

    private final FormClient formLogin = new FormClient();


    public PacAuthenticationServiceTest() {
        formLogin.setName("form");
        formLogin.setAuthenticator(new SimpleAuthenticatorTest().getInstance());
        formLogin.setProfileCreator(new UsernameProfileCreator());

        clientSet.add(formLogin);
        loginProviders.add("form");
        loginProviders.add("basic");
        userIdAttributes.put("twitter", "screen_name");
        MockitoAnnotations.initMocks(this);
        pacAuthenticationService.afterPropertiesSet();
    } // init()


    @Test
    public void testPacAuthenticationService() {
        PacAuthenticationService p = pacAuthenticationService;
        Assert.assertEquals(p.getProviderNames().size(), 1, "There should be one provider name in test.");
        TargetDescriptor target = new TargetDescriptor(p, null, "login");
        Set<String> emptyStringSet = Collections.emptySet();
        Assert.assertEquals(p.getLoginTarget(emptyStringSet), target, "The login target falls back to the default.");

        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();

        Link link = p.createLink(request, response, p, "logout", null);
        Assert.assertNotNull(link, "We need a valid logout link to test");
        Mockito.when(aggregator.createLink(request, response, p, "logout", null)).thenReturn(link);
        Link reTest = aggregator.createLink(request, response, p, "logout", null);
        Assert.assertEquals(reTest, link, "Logout link should be the one just generated.");
        Link logoutLink = p.getLogoutLink(request, response);
        Assert.assertEquals(logoutLink, link, "Logout link should be the one provided by mock.");

        HttpSession session = request.getSession(true);
        Assert.assertNotNull(session, "Fresh session instance expected.");
        Assert.assertEquals(p.getUsers(request, response).size(), 0, "There should be no users in test.");
    } // testPacAuthenticationService()


    @Test
    public void testRedirectToLogin() {
        PacAuthenticationService p = pacAuthenticationService;
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();
        LOG.info("testRedirectToLogin() loginProviders={}", loginProviders);
        Link link = p.createLink(request, response, p, "form", null);
        Assert.assertNotNull(link, "We need a valid logout link to test");
        Mockito.when(aggregator.createLink(request, response, p, "form", null)).thenReturn(link);
        try {
            p.redirectToLogin(request, response, loginProviders);
        } catch (IOException e) {
            Assert.fail("Authentication service should not issue exceptions.", e);
        } // try/catch
        Assert.assertEquals(response.getStatus(), 302, "Redirect to login should issue a redirect response state.");
        Assert.assertEquals(response.getHeader("Location"), "/redirect/form", "Unexpected login redirect target.");
    } // testRedirectToLogin()


    @Test
    public void testRedirect() {
        LOG.info("testRedirect()");
        PacAuthenticationService p = pacAuthenticationService;
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/redirect");
        MockHttpServletResponse response = new MockHttpServletResponse();
        HttpSession session = request.getSession(true);
        Assert.assertNotNull(session, "Fresh session instance expected.");
        Assert.assertEquals(p.getUsers(request, response).size(), 0, "There should be no users in test.");

        Link l = p.createLink(request, response, p, "callback", null);
        Mockito.when(aggregator.createLink(request, response, p, "callback", null)).thenReturn(l);
        l = p.createLink(request, response, p, "login-form", null);
        Mockito.when(aggregator.createLink(request, response, p, "login-form", null)).thenReturn(l);
        TargetDescriptor redirect = null;
        try {
            redirect = p.redirect("form", request, response);
        } catch (Exception e) {
            Assert.fail("Authentication service should not issue exceptions.", e);
        } // try/catch
        LOG.info("testRedirect() session {}", session.getAttributeNames());
        Assert.assertEquals(redirect, TargetDescriptor.DONE, "Redirect handling does not include a view.");
        Assert.assertEquals(response.getStatus(), HttpServletResponse.SC_MOVED_TEMPORARILY, "Unexpected login redirect response code.");
        Assert.assertEquals(response.getHeader("Location"), "/login-form", "Unexpected login redirect target.");
    } // testRedirect()


    @Test
    public void testCallback() {
        LOG.info("testCallback()");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/testapp/callback");
        request.setParameter("client_name", "form");
        request.setParameter(formLogin.getUsernameParameter(), "testuser");
        request.setParameter(formLogin.getPasswordParameter(), "testpassword");
        MockHttpServletResponse response = new MockHttpServletResponse();
        Link l = pacAuthenticationService.createLink(request, response, pacAuthenticationService, "callback", null);
        Mockito.when(aggregator.createLink(request, response, pacAuthenticationService, "callback", null)).thenReturn(l);
        l = pacAuthenticationService.createLink(request, response, pacAuthenticationService, "login-form", null);
        Mockito.when(aggregator.createLink(request, response, pacAuthenticationService, "login-form", null)).thenReturn(l);
        TargetDescriptor login = null;
        try {
            login = pacAuthenticationService.callback(request, response);
        } catch (Exception e) {
            Assert.fail("Authentication service should not issue exceptions.", e);
        } // try/catch
        TargetDescriptor reference = new TargetDescriptor(pacAuthenticationService, null, "return");
        Assert.assertEquals(login, reference, "Expected view for login page.");
        LOG.info("testCallback() done.");
    } // testCallback()


    @Test
    public void testCreateLink() {
        PacAuthenticationService p = pacAuthenticationService;
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();
        Link link = null;
        try {
            link = p.createLink(request, response, p, null, "form");
        } catch (Exception e) {
            Assert.fail("Authentication service should not issue exceptions.", e);
        } // try/catch
        Assert.assertEquals(link.getUrl(), "/login-form", "Expected correct authentication URL.");
        try {
            link = p.createLink(request, response, this, null, "form");
        } catch (Exception e) {
            Assert.fail("Authentication service should not issue exceptions.", e);
        } // try/catch
        Assert.assertNull(link, "Expected correct authentication URL.");
        try {
            link = p.createLink(request, response, p, "return", null);
        } catch (Exception e) {
            Assert.fail("Authentication service should not issue exceptions.", e);
        } // try/catch
        Assert.assertEquals(link.getUrl(), "/", "Expected correct authentication URL.");
        HttpSession session = request.getSession(true);
        Assert.assertNotNull(session, "Fresh session instance expected.");
        session.setAttribute(Constants.ATTRIBUTE_RETURN_URL, "/testapp/magic-return-url");
        Mockito.when(aggregator.getPrefix(request)).thenReturn("/testapp");
        try {
            link = p.createLink(request, response, p, "return", null);
        } catch (Exception e) {
            Assert.fail("Authentication service should not issue exceptions.", e);
        } // try/catch
        Assert.assertEquals(link.getUrl(), "/magic-return-url", "Expected correct authentication URL.");
        try {
            link = p.createLink(request, response, p, "test", null);
        } catch (Exception e) {
            Assert.fail("Authentication service should not issue exceptions.", e);
        } // try/catch
        Assert.assertEquals(link.getUrl(), "/test", "Expected correct authentication URL.");
        try {
            link = p.createLink(request, response, p, null, "wupp");
        } catch (Exception e) {
            Assert.fail("Authentication service should not issue exceptions.", e);
        } // try/catch
        Assert.assertEquals(link.getUrl(), "/login", "Expected correct authentication URL.");
    } // testCreateLink()


    @Test
    public void testFormLogin() {
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();
        TargetDescriptor login = null;
        try {
            login = pacAuthenticationService.form(request, response);
        } catch (Exception e) {
            Assert.fail("Authentication service should not issue exceptions.", e);
        } // try/catch
        TargetDescriptor reference = new TargetDescriptor(pacAuthenticationService, "form", null);
        Assert.assertEquals(login, reference, "Expected view for login page.");
    } // testFormLogin()


    @Test
    public void testLoginPage() {
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();
        TargetDescriptor login = null;
        try {
            login = pacAuthenticationService.login(request, response);
        } catch (Exception e) {
            Assert.fail("Authentication service should not issue exceptions.", e);
        } // try/catch
        TargetDescriptor reference = new TargetDescriptor(pacAuthenticationService, null, null);
        Assert.assertEquals(login, reference, "Expected view for login page.");
    } // testLoginPage()


    @Test
    public void testLogout() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/testapp/logout");
        request.setServletPath("/testapp");
        HttpServletResponse response = new MockHttpServletResponse();
        HttpSession before = request.getSession(true);
        Assert.assertNotNull(before, "Fresh session instance expected.");
        String reference = "/testapp/still-a-return-url";
        before.setAttribute(Constants.ATTRIBUTE_RETURN_URL, reference);
        before.setAttribute("x", reference);
        TargetDescriptor logout = null;
        try {
            logout = pacAuthenticationService.logout(request, response);
        } catch (Exception e) {
            Assert.fail("Authentication service should not issue exceptions.", e);
        } // try/catch
        HttpSession after = request.getSession(false);
        Assert.assertNotNull(after, "There should be a session available after logout.");
        Assert.assertNotEquals(after, before, "Strange session and contents after logout.");
        Assert.assertNull(after.getAttribute("x"), "Strange session and contents after logout.");
        Assert.assertEquals(after.getAttribute(Constants.ATTRIBUTE_RETURN_URL), reference, "Expected view for login page.");
        TargetDescriptor target = new TargetDescriptor(pacAuthenticationService, null, "return");
        Assert.assertEquals(logout, target, "Expected view for logout page.");
    } // testLogout()

} // PacAuthenticationServiceTest
