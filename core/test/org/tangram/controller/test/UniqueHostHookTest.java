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
package org.tangram.controller.test;

import java.util.Collections;
import java.util.Map;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.tangram.controller.UniqueHostHook;
import org.tangram.link.Link;
import org.tangram.link.LinkFactoryAggregator;
import org.tangram.link.TargetDescriptor;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test redirect reaction of unique host hook.
 */
public class UniqueHostHookTest {

    private static final String DOMAIN = "www.example.org";

    private static final String URI = "/s/id_96";

    @Mock
    private final LinkFactoryAggregator aggregator = Mockito.mock(LinkFactoryAggregator.class);

    @InjectMocks
    private final UniqueHostHook hook = new UniqueHostHook();


    @Test
    public void testUniqueHostHook() {
        MockitoAnnotations.initMocks(this);
        MockHttpServletRequest correctRequest = new MockHttpServletRequest("GET", "http://"+DOMAIN+":8080"+URI);
        correctRequest.addHeader("Host", DOMAIN);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "http://www.example.com:8080"+URI);
        request.addHeader("Host", "www.example.com");
        MockHttpServletRequest localRequest = new MockHttpServletRequest("GET", "http://www.example.com:8080"+URI);
        MockHttpServletResponse response = new MockHttpServletResponse();

        Map<String, Object> model = Collections.EMPTY_MAP;
        TargetDescriptor descriptor = new TargetDescriptor(model, "x", "y");
        Link link = new Link(URI);

        Mockito.when(aggregator.createLink(correctRequest, response, descriptor.bean, descriptor.action, descriptor.view)).thenReturn(link);
        Mockito.when(aggregator.createLink(request, response, descriptor.bean, descriptor.action, descriptor.view)).thenReturn(link);
        Mockito.when(aggregator.createLink(localRequest, response, descriptor.bean, descriptor.action, descriptor.view)).thenReturn(link);

        hook.setPrimaryDomain(DOMAIN);

        String exptectedUrl = "http://"+DOMAIN+link.getUrl();
        try {
            boolean intercepted = hook.intercept(descriptor, model, request, response);
            Assert.assertEquals(intercepted, true, "Expected interception of this request for redirect to new domain");
            Assert.assertEquals(response.getHeader("Location"), exptectedUrl, "Unexpected redirect URL");
        } catch (Exception ex) {
            Assert.fail("Exception thrown while test", ex);
        } // try/catch

        try {
            boolean intercepted = hook.intercept(descriptor, model, localRequest, response);
            Assert.assertEquals(intercepted, true, "Local requests should be intercepted");
            Assert.assertEquals(response.getHeader("Location"), exptectedUrl, "Unexpected redirect URL");
        } catch (Exception ex) {
            Assert.fail("Exception thrown while test", ex);
        } // try/catch

        try {
            boolean intercepted = hook.intercept(descriptor, model, correctRequest, response);
            Assert.assertEquals(intercepted, false, "Correct requests should not be intercepted");
        } catch (Exception ex) {
            Assert.fail("Exception thrown while test", ex);
        } // try/catch
    } // testUniqueHostHook

} // UniqueHostHookTest
