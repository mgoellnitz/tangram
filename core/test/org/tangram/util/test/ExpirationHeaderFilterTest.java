/**
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
package org.tangram.util.test;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletResponse;
import org.tangram.util.ExpirationHeaderFilter;
import org.testng.Assert;
import org.testng.annotations.Test;


public class ExpirationHeaderFilterTest {

    @Test
    public void testFilterHandling() {
        FilterConfig config = Mockito.mock(FilterConfig.class);
        Mockito.when(config.getInitParameter("expirations")).thenReturn("xml=604800,html=0,DEFAULT=86400");

        FilterChain chain = Mockito.mock(FilterChain.class);

        String[] testUrls = {"hallo", "test.xml", "test.txt"};

        ExpirationHeaderFilter filter = new ExpirationHeaderFilter();
        try {
            filter.init(config);
            filter.addExpirationTime("txt", 12345);
            boolean expected = false;
            for (String url : testUrls) {
                HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
                HttpServletResponse response = new MockHttpServletResponse();
                Mockito.when(request.getRequestURI()).thenReturn(url);
                filter.doFilter(request, response, chain);
                Assert.assertEquals(response.containsHeader("Expires"), expected, "Unexpected occurence of expiry header "+url+" discovered.");
                expected = true;
            } // for
        } catch (ServletException|IOException e) {
            Assert.fail("Exception on initialization or execution of filter occured.", e);
        } // try/catch
        filter.destroy();
    } // testFilterHandling()

} // ExpirationHeaderFilterTest
