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

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.List;
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
import org.tangram.components.test.GenericCodeResourceCacheTest;
import org.tangram.content.CodeResourceCache;
import org.tangram.monitor.Statistics;
import org.tangram.servlet.JspTemplateResolver;
import org.tangram.servlet.RepositoryTemplateResolver;
import org.tangram.view.DefaultViewContextFactory;
import org.tangram.view.RequestParameterAccess;
import org.tangram.view.TemplateResolver;
import org.tangram.view.velocity.VelocityResourceLoader;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test the servlet view utilities.
 */
public class ServletViewUtilitiesTest {

    @Spy
    private final Statistics statistics = new SimpleStatistics(); // NOPMD - this field is not really unused

    @Spy
    private CodeResourceCache cache;

    @Spy
    private DefaultViewContextFactory viewContextFactory = new DefaultViewContextFactory();

    @InjectMocks
    private final JspTemplateResolver jspTemplateResolver = new JspTemplateResolver();

    @InjectMocks
    private final RepositoryTemplateResolver repositoryTemplateResolver = new RepositoryTemplateResolver();

    @InjectMocks
    private ServletViewUtilities servletViewUtilities = new ServletViewUtilities();


    public ServletViewUtilitiesTest() throws FileNotFoundException {
        MockServletContext servletContext = new MockServletContext(".");
        servletViewUtilities.setResolvers(setupResolvers(servletContext));
        cache = new GenericCodeResourceCacheTest().getInstance();
        VelocityResourceLoader.codeResourceCache = cache;
        MockitoAnnotations.initMocks(this);
    } // ()


    @SuppressWarnings("rawtypes")
    private Set<TemplateResolver> setupResolvers(MockServletContext servletContext) {
        Set<TemplateResolver> resolvers = new HashSet<>();
        jspTemplateResolver.setServletContext(servletContext);
        jspTemplateResolver.afterPropertiesSet();
        resolvers.add(jspTemplateResolver);
        resolvers.add(repositoryTemplateResolver);
        return resolvers;
    } // setupResolvers()


    @Test
    public void testServletViewUtilities() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/testapp/id_RootTopic:1");
        request.setContextPath("/testapp");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setContentType("text/html");

        Object bean = new Throwable("Test Code");
        Map<String, Object> model = viewContextFactory.createModel(bean, request, response);
        servletViewUtilities.render(null, model, null);

        Map<String, Object> secondModel = viewContextFactory.createModel(servletViewUtilities, request, response);
        VelocityResourceLoader.codeResourceCache = cache;
        servletViewUtilities.render(null, secondModel, null);

        List<String> includedUrls = response.getIncludedUrls();
        Assert.assertNotNull(includedUrls, "There must be some include urls.");
        Assert.assertEquals(response.getContentAsString(), "<h1>Title</h1>\n", "The result is empty for mock instances.");
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
