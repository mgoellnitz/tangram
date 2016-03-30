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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.tangram.components.SimpleStatistics;
import org.tangram.components.servlet.ServletViewUtilities;
import org.tangram.monitor.Statistics;
import org.tangram.servlet.JspTemplateResolver;
import org.tangram.view.DefaultViewContextFactory;
import org.tangram.view.RequestParameterAccess;
import org.tangram.view.TemplateResolver;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test the servlet view utilities.
 */
public class ServletViewUtilitiesTest {

    @Spy
    private final Statistics statistics = new SimpleStatistics(); // NOPMD - this field is not really unused

    @InjectMocks
    private final JspTemplateResolver jspTemplateResolver = new JspTemplateResolver();


    @Test
    public void testServletViewUtilities() throws Exception {
        ServletViewUtilities servletViewUtilities = new ServletViewUtilities();
        DefaultViewContextFactory viewContextFactory = new DefaultViewContextFactory();
        MockServletContext servletContext = new MockServletContext(".");

        Set<TemplateResolver> resolvers = new HashSet<>();
        jspTemplateResolver.setServletContext(servletContext);
        jspTemplateResolver.afterPropertiesSet();
        resolvers.add(jspTemplateResolver);
        servletViewUtilities.setResolvers(resolvers);

        MockitoAnnotations.initMocks(this);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/testapp/id_RootTopic:1");
        request.setContextPath("/testapp");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setContentType("text/html");

        Object bean = new Throwable("Test Code");
        Map<String, Object> model = viewContextFactory.createModel(bean, request, response);

        servletViewUtilities.render(null, model, null);
        Assert.assertEquals(response.getContentAsString(), "", "The result is empty for mock instances.");
    } // testServletViewUtilities()


    @Test
    public void testCreateParameterAccess() throws Exception {
        ServletViewUtilities servletViewUtilities = new ServletViewUtilities();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/testapp/id_RootTopic:1");
        request.setContextPath("/testapp");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setContentType("text/html");
        RequestParameterAccess parameterAccess = servletViewUtilities.createParameterAccess(request);
        Assert.assertNotNull(parameterAccess, "Access instance should have been created.");
    } // testCreateParameterAccess()

} // ServletViewUtilitiesTest
