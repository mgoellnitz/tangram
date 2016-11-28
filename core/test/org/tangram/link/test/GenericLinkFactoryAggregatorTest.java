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
package org.tangram.link.test;

import java.lang.reflect.Method;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.tangram.annotate.LinkAction;
import org.tangram.components.SimpleStatistics;
import org.tangram.link.GenericLinkFactoryAggregator;
import org.tangram.link.Link;
import org.tangram.link.LinkFactory;
import org.tangram.link.TargetDescriptor;
import org.tangram.monitor.Statistics;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test aspects of the generic link factory aggregator.
 */
public class GenericLinkFactoryAggregatorTest {

    @Mock
    private final Statistics statistics = new SimpleStatistics();

    @InjectMocks
    private final GenericLinkFactoryAggregator linkFactoryAggregator = new GenericLinkFactoryAggregator();


    public GenericLinkFactoryAggregatorTest() {
        statistics.increase("dummy");
        MockitoAnnotations.initMocks(this);
        linkFactoryAggregator.setDispatcherPath("/x");
    }


    public GenericLinkFactoryAggregator getInstance() {
        return linkFactoryAggregator;
    }


    @LinkAction("/test")
    public TargetDescriptor linkAction() {
        return null;
    } // linkAction()


    @Test
    public void testGenericLinkFactoryAggregator() {
        Method method = linkFactoryAggregator.findMethod(this, "linkAction");
        Assert.assertNotNull(method, "Didn't find expected method.");
        Assert.assertEquals(method.getName(), "linkAction", "Found unexpected method name.");
        Assert.assertEquals(method.getParameterTypes().length, 0, "Discovered unexpected parameter count.");
        // Trigger cached lookup and get same result
        Method cachedMethod = linkFactoryAggregator.findMethod(this, "linkAction");
        Assert.assertEquals(cachedMethod, method, "Both lookups should return the same result.");

        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();
        boolean result = false;
        try {
            linkFactoryAggregator.createLink(request, response, null, null, null);
        } catch (RuntimeException e) {
            result = true;
        } // try/catch
        Assert.assertTrue(result, "Creating links for null beans should throw an exception.");

        MockServletContext context = new MockServletContext();
        context.setContextPath("/");
        request = new MockHttpServletRequest(context);

        String uri = "/id_46";
        Link expectedLink = new Link(linkFactoryAggregator.getDispatcherPath()+uri);
        LinkFactory linkFactory = Mockito.mock(LinkFactory.class);
        Mockito.when(linkFactory.createLink(request, response, this, null, null)).thenReturn(expectedLink);
        linkFactoryAggregator.registerFactory(linkFactory);
        Link l = linkFactoryAggregator.createLink(request, response, this, null, null);
        Assert.assertEquals(l, expectedLink, "We discovered an unexpected link.");

        linkFactoryAggregator.unregisterFactory(linkFactory);
        result = false;
        try {
            linkFactoryAggregator.createLink(request, response, this, null, null);
        } catch (RuntimeException e) {
            result = true;
        } // try/catch
        Assert.assertTrue(result, "An empty collection of link factories should not generate any link.");
    } // testGenericLinkFactoryAggregator()

} // GenericLinkFactoryAggregatorTest
