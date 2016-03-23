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

import java.util.Collections;
import java.util.HashSet;
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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.tangram.Constants;
import org.tangram.components.PacAuthenticationService;
import org.tangram.link.Link;
import org.tangram.link.LinkFactoryAggregator;
import org.tangram.link.LinkHandlerRegistry;
import org.tangram.link.TargetDescriptor;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Test some aspects of the authentication service.
 */
public class PacAuthenticationServiceTest {

    @Mock
    private LinkHandlerRegistry linkHandlerRegistry; // NOPMD - this field is not really unused

    @Mock
    private LinkFactoryAggregator linkFactoryAggregator; // NOPMD - this field is not really unused

    @Spy
    private final Set<Client> clientSet = new HashSet<>(); // NOPMD - this field is not really unused

    @InjectMocks
    private final PacAuthenticationService pacAuthenticationService = new PacAuthenticationService();


    @BeforeClass
    public void init() throws Exception {
        MockitoAnnotations.initMocks(this);
        pacAuthenticationService.afterPropertiesSet();
    } // init()


    @Test
    public void testPacAuthenticationService() {
        PacAuthenticationService p = pacAuthenticationService;
        Assert.assertEquals(p.getProviderNames().size(), 0, "There should be no provider names in test.");
        TargetDescriptor target = new TargetDescriptor(p, null, "login");
        Assert.assertEquals(p.getLoginTarget(Collections.EMPTY_SET), target, "The login target falls back to the default.");

        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();
        HttpSession session = request.getSession(true);
        Assert.assertNotNull(session, "Fresh session instance expected.");
        Assert.assertEquals(p.getUsers(request, response).size(), 0, "There should be no users in test.");
    } // testPacAuthenticationService()


    // TODO:
    public void testCallback() {
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();
        Link link = new Link("/testapp/callback");
        Mockito.when(linkFactoryAggregator.createLink(request, response, pacAuthenticationService, "callback", null)).thenReturn(link);
        TargetDescriptor login = null;
        try {
            login = pacAuthenticationService.callback(request, response);
        } catch (Exception e) {
            Assert.fail("Authentication service should not issue exceptions.", e);
        } // try/catch
        TargetDescriptor reference = new TargetDescriptor(pacAuthenticationService, null, null);
        Assert.assertEquals(login, reference, "Expected view for login page.");
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
        Mockito.when(linkFactoryAggregator.getPrefix(request)).thenReturn("/testapp");
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
