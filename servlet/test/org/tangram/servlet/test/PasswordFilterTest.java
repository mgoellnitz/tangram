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
package org.tangram.servlet.test;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.mockito.Mockito;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.tangram.protection.AuthorizationService;
import org.tangram.servlet.PasswordFilter;
import org.tangram.test.VoidCallCheck;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test aspects of the password checking servlet filter.
 */
public class PasswordFilterTest {

    @Test
    public void testPasswordFilter() throws Exception {
        PasswordFilter passwordFilter = new PasswordFilter();
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();
        AuthorizationService authorizationService = Mockito.mock(AuthorizationService.class);
        VoidCallCheck voidCallCheck = new VoidCallCheck();
        Mockito.doAnswer(voidCallCheck).when(authorizationService).handleRequest(request, response);
        FilterChain chain = new MockFilterChain();
        passwordFilter.setAuthorizationService(authorizationService);
        try {
            passwordFilter.doFilter(request, response, chain);
        } catch (IOException|ServletException e) {
            Assert.fail("doFilter() should not fail.", e);
        } // try/catch

        Assert.assertTrue(voidCallCheck.isCalled(), "The handle request method should have been called.");
    } // testPasswordFilter

} // PasswordFilterTest
