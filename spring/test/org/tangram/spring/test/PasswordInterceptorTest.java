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
package org.tangram.spring.test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.tangram.protection.AuthorizationService;
import org.tangram.spring.PasswordInterceptor;
import org.tangram.test.VoidCallCheck;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test aspects of the password checking spring interceptor.
 */
public class PasswordInterceptorTest {

    @Mock
    private AuthorizationService authorizationService;

    @InjectMocks
    private final PasswordInterceptor passwordInterceptor = new PasswordInterceptor();


    @Test
    public void testPasswordFilter() throws Exception {
        MockitoAnnotations.initMocks(this);
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();
        VoidCallCheck voidCallCheck = new VoidCallCheck();
        Mockito.doAnswer(voidCallCheck).when(authorizationService).handleRequest(request, response);
        try {
            passwordInterceptor.preHandle(request, response, null);
        } catch (Exception e) {
            Assert.fail("preHandle() should not fail.", e);
        } // try/catch

        Assert.assertTrue(voidCallCheck.isCalled(), "The handle request method should have been called.");
    } // testPasswordFilter

} // PasswordFilterTest
