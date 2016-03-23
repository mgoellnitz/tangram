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

import java.util.HashSet;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.tangram.authentication.AuthenticationService;
import org.tangram.components.GenericAuthorizationService;
import org.tangram.content.CodeResourceCache;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Test authorization service behaviour.
 */
public class GenericAuthorizationServiceTest {

    @Mock
    private AuthenticationService authenticationService;  // NOPMD - this field is not really unused

    @Mock
    private CodeResourceCache codeResourceCache; // NOPMD - this field is not really unused

    @Mock(name = "adminUsers")
    private final Set<String> adminUsers = new HashSet<>(); // NOPMD - this field is not really unused

    @InjectMocks
    private final GenericAuthorizationService authorizationService = new GenericAuthorizationService();


    @BeforeClass
    public void init() throws Exception {
        MockitoAnnotations.initMocks(this);
        authorizationService.afterPropertiesSet();
    } // init()


    @Test
    public void testGenericAuthorizationService() {
        // TODO:
        // authorizationService.reset();
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();
        Assert.assertFalse(authorizationService.isAdminUser(request, response), "Dummy request don't belong to admin users.");

        boolean result = false;
        try {
            authorizationService.throwIfNotAdmin(request, response, "Sorry");
        } catch (Exception e) {
            result = true;
        } // try/catch
        Assert.assertTrue(result, "Since dummy request contains no admin user an exception should be thrown.");
    } // testGenericAuthorizationService()

} // GenericAuthorizationServiceTest
