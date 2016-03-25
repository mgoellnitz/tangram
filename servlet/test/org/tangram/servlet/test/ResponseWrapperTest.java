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
import javax.servlet.http.HttpServletResponse;
import org.springframework.mock.web.MockHttpServletResponse;
import org.tangram.servlet.ResponseWrapper;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test aspects of the response wrapper.
 */
public class ResponseWrapperTest {

    @Test
    public void testResponseWrapper() {
        HttpServletResponse response = new MockHttpServletResponse();
        ResponseWrapper responseWrapper = new ResponseWrapper(response);
        Assert.assertEquals(responseWrapper.getContentType(), null, "Initial content type expected.");
        responseWrapper.setContentType("text/html");
        Assert.assertEquals(responseWrapper.getContentType(), "text/html", "Modified content type expected.");
        Assert.assertEquals(responseWrapper.getHeaders().size(), 0, "No initial headers expected.");
        responseWrapper.addHeader("a", "b");
        Assert.assertEquals(responseWrapper.getHeaders().size(), 1, "Added header value not found.");
        Assert.assertEquals(responseWrapper.getHeaders("a").size(), 1, "Added header value not found.");
        responseWrapper.addIntHeader("a", 42);
        Assert.assertEquals(responseWrapper.getHeaders().size(), 1, "Added header values not found.");
        Assert.assertEquals(responseWrapper.getHeaders("a").size(), 2, "Added header values not found.");
        Assert.assertEquals(responseWrapper.getHeader("a"), "b", "Added header values not found.");
        responseWrapper.setHeader("a", "c");
        Assert.assertEquals(responseWrapper.getHeaders("a").size(), 1, "Added header values not found.");
        Assert.assertEquals(responseWrapper.getHeader("a"), "c", "Added header values not found.");
        responseWrapper.setIntHeader("a", 19);
        Assert.assertEquals(responseWrapper.getHeaders("a").size(), 1, "Added header values not found.");
        Assert.assertEquals(responseWrapper.getHeader("a"), "19", "Added header values not found.");
        Assert.assertFalse(responseWrapper.containsHeader("date"), "We didn't add a date header so far.");
        responseWrapper.addDateHeader("date", 135678896456L);
        Assert.assertEquals(responseWrapper.getHeaders().size(), 1, "Added date header values not found.");
        Assert.assertEquals(responseWrapper.getHeaders("date").size(), 1, "Added date header values not found.");
        responseWrapper.addDateHeader("date", 145678896456L);
        Assert.assertEquals(responseWrapper.getHeaders().size(), 1, "Added date header values not found.");
        Assert.assertEquals(responseWrapper.getHeaders("date").size(), 2, "Added date header values not found.");
        responseWrapper.setDateHeader("date", 145678543453456L);
        Assert.assertEquals(responseWrapper.getHeaders().size(), 1, "Added date header values not found.");
        Assert.assertEquals(responseWrapper.getHeaders("date").size(), 1, "Added date header values not found.");
        try {
            responseWrapper.flushBuffer();
        } catch (IOException e) {
            Assert.fail("flushBuffer should not fail.", e);
        } // try/catch
    } // testResponseWrapper()

} // ResponseWrapperTest
