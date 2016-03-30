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

import java.util.HashMap;
import java.util.Map;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.pac4j.http.credentials.UsernamePasswordCredentials;
import org.tangram.components.SimpleAuthenticator;
import org.tangram.content.CodeResource;
import org.tangram.content.CodeResourceCache;
import org.tangram.content.TransientCode;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test this simple credential checker.
 */
public class SimpleAuthenticatorTest {

    @Mock(name = "usernamePasswordMapping")
    private final Map<String, String> usernamePasswordMapping = new HashMap<>(); // NOPMD - this field is not really unused

    @Mock
    private final CodeResourceCache codeCache = Mockito.mock(CodeResourceCache.class);

    @InjectMocks
    private final SimpleAuthenticator simpleAuthenticator = new SimpleAuthenticator();


    public SimpleAuthenticatorTest() {
        usernamePasswordMapping.put("hinz", "kunz");

        String passwords = "testuser=9f735e0df9a1ddc702bf0a1a7b83033f9f7153a00c29de82cedadc9957289b05";
        TransientCode code = new TransientCode("users.properties", "text/plain", "Code:42", passwords, 12345);
        Map<String, CodeResource> codes = new HashMap<>();
        codes.put("users.properties", code);
        Assert.assertEquals(codes.size(), 1, "We should have exactly one resource prepared.");

        MockitoAnnotations.initMocks(this);
        Mockito.when(codeCache.getTypeCache("text/plain")).thenReturn(codes);
    } // ()


    /**
     * Return the instance in test with every mock needed for other tests to include a working instance.
     *
     * @return code resource cache to be used in other tests
     */
    public SimpleAuthenticator getInstance() {
        return simpleAuthenticator;
    } // getInstance()


    @Test
    public void testSimpleAuthenticator() {
        Assert.assertEquals(codeCache.getTypeCache("text/plain").size(), 1, "We should have exactly one resource in the cache.");

        UsernamePasswordCredentials c = new UsernamePasswordCredentials("testuser", "testpassword", "dontcare");
        simpleAuthenticator.validate(c);
        c = new UsernamePasswordCredentials("testuser", "wrong", "dontcare");
        boolean result = false;
        try {
            simpleAuthenticator.validate(c);
        } catch (RuntimeException e) {
            result = true;
        } // try/catch
        Assert.assertTrue(result, "Invalid logins should throw an exception.");

        c = new UsernamePasswordCredentials("testguy", "irrelevant", "dontcare");
        result = false;
        try {
            simpleAuthenticator.validate(c);
        } catch (RuntimeException e) {
            result = true;
        } // try/catch
        Assert.assertTrue(result, "Invalid user should throw an exception.");
    } // testSimpleAuthenticator()

} // SimpleAuthenticatorTest
