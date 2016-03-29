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
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletResponse;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.tangram.authentication.AuthenticationService;
import org.tangram.authentication.GenericUser;
import org.tangram.authentication.User;
import org.tangram.components.GenericAuthorizationService;
import org.tangram.content.CodeResourceCache;
import org.tangram.link.TargetDescriptor;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Test authorization service behaviour.
 */
public class GenericAuthorizationServiceTest {

    @Mock
    private AuthenticationService authenticationService;  // NOPMD - this field is not really unused

    @Spy
    private CodeResourceCache codeResourceCache; // NOPMD - this field is not really unused

    @Spy
    private final Set<String> adminUsers = new HashSet<>(); // NOPMD - this field is not really unused

    @Spy
    private final Set<String> allowedUsers = new HashSet<>(); // NOPMD - this field is not really unused

    @Spy
    private final Set<String> freeUrls = new HashSet<>(); // NOPMD - this field is not really unused

    @Spy
    private final Set<String> loginProviders = new HashSet<>(); // NOPMD - this field is not really unused

    @InjectMocks
    private final GenericAuthorizationService authorizationService = new GenericAuthorizationService();


    @BeforeClass
    public void init() throws Exception {
        GenericCodeResourceCacheTest codeResourceCacheTest = new GenericCodeResourceCacheTest();
        codeResourceCacheTest.init("/auth-content.xml");
        codeResourceCache = codeResourceCacheTest.getInstance();
        freeUrls.add("/free");
        adminUsers.add("form:admin");
        allowedUsers.add("form:testuser");
        loginProviders.add("form");
        MockitoAnnotations.initMocks(this);
        authorizationService.afterPropertiesSet();
    } // init()


    @Test
    public void testGenericAuthorizationService() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();
        Assert.assertFalse(authorizationService.isAdminUser(request, response), "Dummy request don't belong to admin users.");

        boolean result = false;
        try {
            authorizationService.throwIfNotAdmin(request, response, "Sorry");
        } catch (Exception e) {
            result = true;
        } // try/catch
        Assert.assertTrue(result, "Since dummy request contains no admin user an exception should be thrown.");
        Map<String, Object> properties = Collections.EMPTY_MAP;
        GenericUser user = new GenericUser("form", "testuser", properties);
        Set<User> users = new HashSet<>();
        users.add(user);
        Mockito.when(authenticationService.getUsers(request, response)).thenReturn(users);
        try {
            authorizationService.throwIfNotAdmin(request, response, "Sorry");
        } catch (Exception e) {
            result = false;
        } // try/catch
        Assert.assertTrue(result, "Now the user should have been authorized.");

        TargetDescriptor loginTarget = new TargetDescriptor(this, "log", "in");
        Mockito.when(authenticationService.getLoginTarget(loginProviders)).thenReturn(loginTarget);
        TargetDescriptor target = authorizationService.getLoginTarget(request);
        Assert.assertEquals(target, loginTarget, "We need a login target.");
    } // testGenericAuthorizationService()

} // GenericAuthorizationServiceTest
